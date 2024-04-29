package xyz.migoo.framework.infra.service.sys.permission;

import cn.hutool.core.collection.CollUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.util.collection.CollectionUtils;
import xyz.migoo.framework.infra.controller.sys.permission.menu.vo.MenuQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Menu;
import xyz.migoo.framework.infra.dal.mapper.sys.MenuMapper;
import xyz.migoo.framework.infra.enums.MenuIdEnum;
import xyz.migoo.framework.infra.enums.MenuTypeEnum;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.migoo.framework.infra.enums.ErrorCodeConstants.*;

@Service
@Slf4j
public class MenuServiceImpl implements MenuService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 菜单缓存
     * key：菜单编号
     * <p>
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<Long, Menu> menuCache;
    /**
     * 权限与菜单缓存
     * key：权限 {@link Menu#getPermission()}
     * value：SysMenuDO 数组，因为一个权限可能对应多个 SysMenuDO 对象
     * <p>
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<String, Menu> permissionMenuCache;
    /**
     * 缓存菜单的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile LocalDateTime maxUpdateTime;

    @Resource
    private MenuMapper menuMapper;
    @Resource
    @Lazy
    private PermissionService permissionService;

    /**
     * 初始化 {@link #menuCache} 和 {@link #permissionMenuCache} 缓存
     */
    @Override
    @PostConstruct
    public synchronized void initLocalCache() {
        // 获取菜单列表，如果有更新
        List<Menu> menuList = this.loadMenuIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(menuList)) {
            return;
        }

        // 构建缓存
        ImmutableMap.Builder<Long, Menu> menuCacheBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder<String, Menu> permMenuCacheBuilder = ImmutableMultimap.builder();
        menuList.forEach(menuDO -> {
            menuCacheBuilder.put(menuDO.getId(), menuDO);
            permMenuCacheBuilder.put(menuDO.getPermission(), menuDO);
        });
        menuCache = menuCacheBuilder.build();
        permissionMenuCache = permMenuCacheBuilder.build();
        assert !menuList.isEmpty();
        maxUpdateTime = menuList.stream().max(Comparator.comparing(Menu::getUpdateTime)).get().getUpdateTime();
        log.info("[initLocalCache][缓存菜单，数量为:{}]", menuList.size());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCache();
    }

    /**
     * 如果菜单发生变化，从数据库中获取最新的全量菜单。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前菜单的最大更新时间
     * @return 菜单列表
     */
    private List<Menu> loadMenuIfUpdate(LocalDateTime maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) {
            // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadMenuIfUpdate][首次加载全量菜单]");
        } else { // 判断数据库中是否有更新的菜单
            if (!menuMapper.selectExistsByUpdateTimeAfter(maxUpdateTime)) {
                return null;
            }
            log.info("[loadMenuIfUpdate][增量加载全量菜单]");
        }
        // 第二步，如果有更新，则从数据库加载所有菜单
        return menuMapper.selectList();
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long menuId) {
        // 校验是否还有子菜单
        if (menuMapper.selectCountByParentId(menuId) > 0) {
            throw ServiceExceptionUtil.get(MENU_EXISTS_CHILDREN);
        }
        // 校验删除的菜单是否存在
        if (menuMapper.selectById(menuId) == null) {
            throw ServiceExceptionUtil.get(MENU_NOT_EXISTS);
        }
        // 标记删除
        menuMapper.deleteById(menuId);
        // 删除授予给角色的权限
        permissionService.processMenuDeleted(menuId);
    }

    @Override
    public List<Menu> get(MenuQueryReqVO reqVO) {
        return menuMapper.selectList(reqVO);
    }

    @Override
    public List<Menu> get() {
        return menuMapper.selectList();
    }

    @Override
    public List<Menu> listMenusFromCache(Collection<Integer> menuTypes, Collection<Integer> menusStatuses) {
        // 任一一个参数为空，则返回空
        if (CollectionUtils.isAnyEmpty(menuTypes, menusStatuses)) {
            return Collections.emptyList();
        }
        // 创建新数组，避免缓存被修改
        return menuCache.values().stream().filter(menu -> menuTypes.contains(menu.getType())
                        && menusStatuses.contains(menu.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Menu> listMenusFromCache(Collection<Long> menuIds, Collection<Integer> menuTypes,
                                         Collection<Integer> menusStatuses) {
        // 任一一个参数为空，则返回空
        if (CollectionUtils.isAnyEmpty(menuIds, menuTypes, menusStatuses)) {
            return Collections.emptyList();
        }
        return menuCache.values().stream().filter(menu -> menuIds.contains(menu.getId())
                        && menuTypes.contains(menu.getType())
                        && menusStatuses.contains(menu.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Menu> getMenuListByPermissionFromCache(String permission) {
        return Lists.newArrayList(permissionMenuCache.get(permission));
    }

    @Override
    public Menu get(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    public void add(Menu menu) {
        // 校验父菜单存在
        checkParentResource(menu.getParentId(), null);
        // 校验菜单（自己）
        checkResource(menu.getParentId(), menu.getName(), null);
        // 插入数据库
        initMenuProperty(menu);
        menuMapper.insert(menu);
    }

    @Override
    public void update(Menu menu) {
        // 校验更新的菜单是否存在
        if (menuMapper.selectById(menu.getId()) == null) {
            throw ServiceExceptionUtil.get(MENU_NOT_EXISTS);
        }
        // 校验父菜单存在
        checkParentResource(menu.getParentId(), menu.getId());
        // 校验菜单（自己）
        checkResource(menu.getParentId(), menu.getName(), menu.getId());
        // 更新到数据库
        initMenuProperty(menu);
        menuMapper.updateById(menu);
    }

    /**
     * 校验父菜单是否合法
     * <p>
     * 1. 不能设置自己为父菜单
     * 2. 父菜单不存在
     * 3. 父菜单必须是 {@link MenuTypeEnum#MENU} 菜单类型
     *
     * @param parentId 父菜单编号
     * @param childId  当前菜单编号
     */
    @VisibleForTesting
    public void checkParentResource(Long parentId, Long childId) {
        if (parentId == null || MenuIdEnum.ROOT.getId().equals(parentId)) {
            return;
        }
        // 不能设置自己为父菜单
        if (parentId.equals(childId)) {
            throw ServiceExceptionUtil.get(MENU_PARENT_ERROR);
        }
        Menu menu = menuMapper.selectById(parentId);
        // 父菜单不存在
        if (menu == null) {
            throw ServiceExceptionUtil.get(MENU_PARENT_NOT_EXISTS);
        }
        // 父菜单必须是目录或者菜单类型
        if (!MenuTypeEnum.DIR.getType().equals(menu.getType())
                && !MenuTypeEnum.MENU.getType().equals(menu.getType())) {
            throw ServiceExceptionUtil.get(MENU_PARENT_NOT_DIR_OR_MENU);
        }
    }

    /**
     * 校验菜单是否合法
     * <p>
     * 1. 校验相同父菜单编号下，是否存在相同的菜单名
     *
     * @param name     菜单名字
     * @param parentId 父菜单编号
     * @param id       菜单编号
     */
    @VisibleForTesting
    public void checkResource(Long parentId, String name, Long id) {
        Menu menu = menuMapper.selectByParentIdAndName(parentId, name);
        if (menu == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的菜单
        if (id == null) {
            throw ServiceExceptionUtil.get(MENU_NAME_DUPLICATE);
        }
        if (!menu.getId().equals(id)) {
            throw ServiceExceptionUtil.get(MENU_NAME_DUPLICATE);
        }
    }

    /**
     * 初始化菜单的通用属性。
     * <p>
     * 例如说，只有目录或者菜单类型的菜单，才设置 icon
     *
     * @param menu 菜单
     */
    private void initMenuProperty(Menu menu) {
        // 菜单为按钮类型时，无需 component、icon、path 属性，进行置空
        if (MenuTypeEnum.BUTTON.getType().equals(menu.getType())) {
            menu.setComponent("");
            menu.setIcon("");
            menu.setPath("");
        }
    }

}
