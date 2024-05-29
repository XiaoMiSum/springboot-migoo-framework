package xyz.migoo.framework.infra.service.sys.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.sys.permission.role.vo.RoleQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Role;
import xyz.migoo.framework.infra.dal.mapper.sys.RoleMapper;
import xyz.migoo.framework.infra.enums.RoleCodeEnum;
import xyz.migoo.framework.infra.enums.RoleTypeEnum;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.migoo.framework.infra.enums.SysErrorCodeConstants.*;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 角色缓存
     * key：角色编号 {@link Role#getId()}
     * <p>
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<Long, Role> roleCache;
    /**
     * 缓存角色的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile LocalDateTime maxUpdateTime;

    @Resource
    @Lazy
    private PermissionService permissionService;

    @Resource
    private RoleMapper roleMapper;

    /**
     * 初始化 {@link #roleCache} 缓存
     */
    @Override
    @PostConstruct
    public void initLocalCache() {
        // 获取角色列表，如果有更新
        List<Role> roleList = this.loadRoleIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(roleList)) {
            return;
        }

        // 写入缓存
        ImmutableMap.Builder<Long, Role> builder = ImmutableMap.builder();
        roleList.forEach(sysRoleDO -> builder.put(sysRoleDO.getId(), sysRoleDO));
        roleCache = builder.build();
        assert !roleList.isEmpty(); // 断言，避免告警
        maxUpdateTime = roleList.stream().max(Comparator.comparing(Role::getUpdateTime)).get().getUpdateTime();
        log.info("[initLocalCache][初始化 Role 数量为 {}]", roleList.size());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCache();
    }

    /**
     * 如果角色发生变化，从数据库中获取最新的全量角色。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前角色的最大更新时间
     * @return 角色列表
     */
    private List<Role> loadRoleIfUpdate(LocalDateTime maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadRoleIfUpdate][首次加载全量角色]");
        } else { // 判断数据库中是否有更新的角色
            if (!roleMapper.selectExistsByUpdateTimeAfter(maxUpdateTime)) {
                return null;
            }
            log.info("[loadRoleIfUpdate][增量加载全量角色]");
        }
        // 第二步，如果有更新，则从数据库加载所有角色
        return roleMapper.selectList();
    }


    @Override
    public void updateRoleDataScope(Long id, Integer dataScope, Set<Long> dataScopeDeptIds) {
        // 校验是否可以更新
        verify(id);
        // 更新数据范围
        Role updateObject = new Role();
        updateObject.setId(id);
        roleMapper.updateById(updateObject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        // 校验是否可以更新
        this.verify(id);
        // 标记删除
        roleMapper.deleteById(id);
        // 删除相关数据
        permissionService.processRoleDeleted(id);
    }

    @Override
    public Role getRoleFromCache(Long id) {
        return roleCache.get(id);
    }

    @Override
    public List<Role> getRoles(@Nullable Collection<Integer> statuses) {
        return roleMapper.selectListByStatus(statuses);
    }

    @Override
    public List<Role> getRolesFromCache(Collection<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return roleCache.values().stream().filter(roleDO -> ids.contains(roleDO.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasAnyAdmin(Collection<Role> roleList) {
        if (CollectionUtil.isEmpty(roleList)) {
            return false;
        }
        return roleList.stream().anyMatch(roleDO -> RoleCodeEnum.SUPER_ADMIN.getKey().equals(roleDO.getCode()));
    }

    @Override
    public Role get(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public List<Role> getList(Integer status) {
        return roleMapper.selectListByStatus(CollectionUtil.newHashSet(status));
    }

    @Override
    public PageResult<Role> getPage(RoleQueryReqVO req) {
        return roleMapper.selectPage(req);
    }

    @Override
    public void verify(String code, String name, Long id) {
        Role role = roleMapper.selectByName(name);
        if (role != null && !role.getId().equals(id)) {
            throw ServiceExceptionUtil.get(ROLE_NAME_DUPLICATE, name);
        }
        // 2. 是否存在相同编码的角色
        if (!StringUtils.hasText(code)) {
            return;
        }
        // 该 code 编码被其它角色所使用
        role = roleMapper.selectByCode(code);
        if (role != null && !role.getId().equals(id)) {
            throw ServiceExceptionUtil.get(ROLE_CODE_DUPLICATE, code);
        }
    }

    @Override
    public void add(Role role) {
        role.setType(RoleTypeEnum.CUSTOM.getType());
        roleMapper.insert(role);
    }

    @Override
    public void update(Role role) {
        this.verify(role.getId());
        roleMapper.updateById(role);
    }

    private void verify(Long id) {
        Role roleDO = roleMapper.selectById(id);
        if (roleDO == null) {
            throw ServiceExceptionUtil.get(ROLE_NOT_EXISTS);
        }
        // 内置角色，不允许删除
        if (RoleTypeEnum.SYSTEM.getType().equals(roleDO.getType())) {
            throw ServiceExceptionUtil.get(ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE);
        }
    }
}
