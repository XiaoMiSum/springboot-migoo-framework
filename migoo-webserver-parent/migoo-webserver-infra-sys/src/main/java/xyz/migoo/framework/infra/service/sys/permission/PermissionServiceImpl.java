package xyz.migoo.framework.infra.service.sys.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.migoo.framework.common.util.collection.CollectionUtils;
import xyz.migoo.framework.common.util.collection.MapUtils;
import xyz.migoo.framework.common.util.collection.SetUtils;
import xyz.migoo.framework.infra.dal.dataobject.sys.Menu;
import xyz.migoo.framework.infra.dal.dataobject.sys.Role;
import xyz.migoo.framework.infra.dal.dataobject.sys.RoleMenu;
import xyz.migoo.framework.infra.dal.dataobject.sys.UserRole;
import xyz.migoo.framework.infra.dal.mapper.sys.RoleMenuMapper;
import xyz.migoo.framework.infra.dal.mapper.sys.UserRoleMapper;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

import java.time.LocalDateTime;
import java.util.*;

import static xyz.migoo.framework.common.enums.CommonStatus.enabled;

@Service("ss")
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 角色编号与菜单编号的缓存映射
     * key：角色编号
     * value：菜单编号的数组
     * <p>
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<Long, Long> roleMenuCache;
    /**
     * 菜单编号与角色编号的缓存映射
     * key：菜单编号
     * value：角色编号的数组
     * <p>
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<Long, Long> menuRoleCache;
    /**
     * 缓存菜单的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile LocalDateTime maxUpdateTime;

    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    @Lazy
    private RoleService roleService;
    @Resource
    @Lazy
    private MenuService menuService;

    /**
     * 初始化 {@link #roleMenuCache} 和 {@link #menuRoleCache} 缓存
     */
    @Override
    @PostConstruct
    public void initLocalCache() {
        LocalDateTime now = LocalDateTime.now();
        // 获取角色与菜单的关联列表，如果有更新
        List<RoleMenu> roleMenuList = this.loadRoleMenuIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(roleMenuList)) {
            return;
        }

        // 初始化 roleMenuCache 和 menuRoleCache 缓存
        ImmutableMultimap.Builder<Long, Long> roleMenuCacheBuilder = ImmutableMultimap.builder();
        ImmutableMultimap.Builder<Long, Long> menuRoleCacheBuilder = ImmutableMultimap.builder();
        roleMenuList.forEach(roleMenuDO -> {
            roleMenuCacheBuilder.put(roleMenuDO.getRoleId(), roleMenuDO.getMenuId());
            menuRoleCacheBuilder.put(roleMenuDO.getMenuId(), roleMenuDO.getRoleId());
        });
        roleMenuCache = roleMenuCacheBuilder.build();
        menuRoleCache = menuRoleCacheBuilder.build();
        assert !roleMenuList.isEmpty();
        maxUpdateTime = now;
        log.info("[initLocalCache][初始化角色与菜单的关联数量为 {}]", roleMenuList.size());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCache();
    }

    /**
     * 如果角色与菜单的关联发生变化，从数据库中获取最新的全量角色与菜单的关联。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前角色与菜单的关联的最大更新时间
     * @return 角色与菜单的关联列表
     */
    private List<RoleMenu> loadRoleMenuIfUpdate(LocalDateTime maxUpdateTime) {
        // 第一步，判断是否要更新，如果更新时间为空，说明 DB 一定有新数据
        if (maxUpdateTime == null) {
            log.info("[loadRoleMenuIfUpdate][首次加载全量角色与菜单的关联]");
        } else { // 判断数据库中是否有更新的角色与菜单的关联
            if (Objects.isNull(roleMenuMapper.selectExistsByUpdateTimeAfter(maxUpdateTime))) {
                return null;
            }
            log.info("[loadRoleMenuIfUpdate][增量加载全量角色与菜单的关联]");
        }
        // 第二步，如果有更新，则从数据库加载所有角色与菜单的关联
        return roleMenuMapper.selectList();
    }

    @Override
    public List<Menu> getRoleMenusFromCache(Collection<Long> roleIds, Collection<Integer> menuTypes,
                                            Collection<Integer> menusStatuses) {
        // 任一一个参数为空时，不返回任何菜单
        if (CollectionUtils.isAnyEmpty(roleIds, menusStatuses, menusStatuses)) {
            return Collections.emptyList();
        }
        // 判断角色是否包含管理员 获得角色拥有的菜单关联
        if (roleService.hasAnyAdmin(roleService.getRolesFromCache(roleIds))) {
            return menuService.listMenusFromCache(menuTypes, menusStatuses);
        }
        List<Long> menuIds = MapUtils.getList(roleMenuCache, roleIds);
        return menuService.listMenusFromCache(menuIds, menuTypes, menusStatuses);
    }

    @Override
    public Set<Long> getUserRoleIds(Long userId, Collection<Integer> roleStatuses) {
        List<UserRole> userRoleList = userRoleMapper.selectListByUserId(userId);
        // 过滤角色状态
        if (CollectionUtil.isNotEmpty(roleStatuses)) {
            userRoleList.removeIf(userRoleDO -> {
                Role role = roleService.getRoleFromCache(userRoleDO.getRoleId());
                return role == null || !roleStatuses.contains(role.getStatus());
            });
        }
        return CollectionUtils.convertSet(userRoleList, UserRole::getRoleId);
    }

    @Override
    public Set<Long> getRoleMenuIds(Long roleId) {
        // 如果是管理员的情况下，获取全部菜单编号
        Role role = roleService.get(roleId);
        if (roleService.hasAnyAdmin(Collections.singletonList(role))) {
            return CollectionUtils.convertSet(menuService.get(), Menu::getId);
        }
        // 如果是非管理员的情况下，获得拥有的菜单编号
        return CollectionUtils.convertSet(roleMenuMapper.selectListByRoleId(roleId),
                RoleMenu::getMenuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleMenu(Long roleId, Set<Long> menuIds) {
        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = CollectionUtils.convertSet(roleMenuMapper.selectListByRoleId(roleId),
                RoleMenu::getMenuId);
        // 计算新增和删除的菜单编号
        Collection<Long> createMenuIds = CollUtil.subtract(menuIds, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIds);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (!CollectionUtil.isEmpty(createMenuIds)) {
            roleMenuMapper.insertList(roleId, createMenuIds);
        }
        if (!CollectionUtil.isEmpty(deleteMenuIds)) {
            roleMenuMapper.deleteListByRoleIdAndMenuIds(roleId, deleteMenuIds);
        }
        initLocalCache();
    }

    @Override
    public Set<Long> getUserRoleIs(Long userId) {
        return CollectionUtils.convertSet(userRoleMapper.selectListByUserId(userId),
                UserRole::getRoleId);
    }

    @Override
    public void assignUserRole(Long userId, Set<Long> roleIds) {
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = CollectionUtils.convertSet(userRoleMapper.selectListByUserId(userId),
                UserRole::getRoleId);
        // 计算新增和删除的角色编号
        Collection<Long> createRoleIds = CollUtil.subtract(roleIds, dbRoleIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbRoleIds, roleIds);
        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollectionUtil.isEmpty(createRoleIds)) {
            userRoleMapper.insertList(userId, createRoleIds);
        }
        if (!CollectionUtil.isEmpty(deleteMenuIds)) {
            userRoleMapper.deleteListByUserIdAndRoleIdIds(userId, deleteMenuIds);
        }
        initLocalCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRoleDeleted(Long roleId) {
        // 标记删除 UserRole
        userRoleMapper.deleteListByRoleId(roleId);
        // 标记删除 RoleMenu
        roleMenuMapper.deleteListByRoleId(roleId);
        initLocalCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processMenuDeleted(Long menuId) {
        roleMenuMapper.deleteListByMenuId(menuId);
        initLocalCache();
    }

    @Override
    public void processUserDeleted(Long userId) {
        userRoleMapper.deleteListByUserId(userId);
        initLocalCache();
    }

    @Override
    public boolean hasPermission(String permission) {
        return hasAnyPermissions(permission);
    }

    @Override
    public boolean hasAnyPermissions(String... permissions) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(permissions)) {
            return true;
        }
        // 获得当前登录的用户。如果为空，说明没有权限
        if (Objects.isNull(SecurityFrameworkUtils.getLoginUser())) {
            return false;
        }
        Set<Long> roleIds = getUserRoleIds(SecurityFrameworkUtils.getLoginUserId(), SetUtils.asSet(enabled.status()));
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }
        // 判断是否是超管。如果是，当然符合条件
        if (roleService.hasAnyAdmin(roleIds)) {
            return true;
        }
        // 遍历权限，判断是否有一个满足
        return Arrays.stream(permissions).anyMatch(permission -> {
            List<Menu> menuList = menuService.getMenuListByPermissionFromCache(permission);
            // 采用严格模式，如果权限找不到对应的 Menu 的话，认为
            if (CollUtil.isEmpty(menuList)) {
                return false;
            }
            // 获得是否拥有该权限，任一一个
            return menuList.stream().anyMatch(menu -> CollUtil.containsAny(roleIds,
                    menuRoleCache.get(menu.getId())));
        });
    }

    @Override
    public boolean hasRole(String role) {
        return hasAnyRoles(role);
    }

    @Override
    public boolean hasAnyRoles(String... roles) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(roles)) {
            return true;
        }
        // 获得当前登录的用户。如果为空，说明没有权限
        if (Objects.isNull(SecurityFrameworkUtils.getLoginUser())) {
            return false;
        }
        Set<Long> roleIds = getUserRoleIds(SecurityFrameworkUtils.getLoginUserId(), SetUtils.asSet(enabled.status()));
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }

        // 判断是否是超管。如果是，当然符合条件
        if (roleService.hasAnyAdmin(roleIds)) {
            return true;
        }
        Set<String> userRoles = CollectionUtils.convertSet(roleService.getRolesFromCache(roleIds),
                Role::getCode);
        return CollUtil.containsAny(userRoles, Sets.newHashSet(roles));
    }


}
