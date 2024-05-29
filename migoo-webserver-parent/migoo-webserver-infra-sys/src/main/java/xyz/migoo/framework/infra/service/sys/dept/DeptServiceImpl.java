package xyz.migoo.framework.infra.service.sys.dept;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.enums.CommonStatusEnum;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.infra.controller.sys.dept.vo.DeptQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Dept;
import xyz.migoo.framework.infra.dal.mapper.sys.DeptMapper;
import xyz.migoo.framework.infra.enums.DeptIdEnum;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

import java.time.LocalDateTime;
import java.util.*;

import static xyz.migoo.framework.infra.enums.SysErrorCodeConstants.*;

@Service
@Slf4j
public class DeptServiceImpl implements DeptService {

    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    @SuppressWarnings("FieldCanBeLocal")
    private volatile Map<Long, Dept> deptCache;
    private volatile Multimap<Long, Dept> parentDeptCache;
    private volatile LocalDateTime maxUpdateTime;

    @Resource
    private DeptMapper deptMapper;

    @Override
    @PostConstruct
    public synchronized void initLocalCache() {
        // 获取部门列表，如果有更新
        List<Dept> deptList = this.loadDeptIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(deptList)) {
            return;
        }
        // 构建缓存
        ImmutableMap.Builder<Long, Dept> builder = ImmutableMap.builder();
        ImmutableMultimap.Builder<Long, Dept> parentBuilder = ImmutableMultimap.builder();
        deptList.forEach(dept -> {
            builder.put(dept.getId(), dept);
            parentBuilder.put(dept.getParentId(), dept);
        });
        // 设置缓存
        deptCache = builder.build();
        parentDeptCache = parentBuilder.build();
        assert !deptList.isEmpty();
        maxUpdateTime = deptList.stream().max(Comparator.comparing(BaseDO::getUpdateTime)).get().getUpdateTime();
        log.info("[initLocalCache][初始化 Dept 数量为 {}]", deptList.size());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCache();
    }

    private List<Dept> loadDeptIfUpdate(LocalDateTime maxUpdateTime) {
        // 第一步，判断是否要更新。 // 如果更新时间为空，说明 DB 一定有新数据
        if (maxUpdateTime == null) {
            log.info("[loadMenuIfUpdate][首次加载全量部门]");
        } else { // 判断数据库中是否有更新的部门
            if (!deptMapper.selectExistsByUpdateTimeAfter(maxUpdateTime)) {
                return null;
            }
            log.info("[loadMenuIfUpdate][增量加载全量部门]");
        }
        // 第二步，如果有更新，则从数据库加载所有部门
        return deptMapper.selectList();
    }

    @Override
    public List<Dept> getList(Collection<Long> ids) {
        return deptMapper.selectBatchIds(ids);
    }

    @Override
    public List<Dept> getList(DeptQueryReqVO req) {
        return deptMapper.selectList(req);
    }

    @Override
    public void add(Dept dept) {
        deptMapper.insert(dept);
    }

    @Override
    public void update(Dept dept) {
        deptMapper.updateById(dept);
    }

    @Override
    public void verify(Long id, Long parentId, String name) {
        // 校验自己存在
        checkDeptExists(id);
        // 校验父部门的有效性
        checkParentDeptEnable(id, parentId);
        // 校验部门名的唯一性
        checkDeptNameUnique(id, parentId, name);
    }

    @Override
    public Dept get(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public void remove(Long id) {
        deptMapper.deleteById(id);
    }

    public List<Dept> getDeptsByParentIdFromCache(Long parentId, boolean recursive) {
        List<Dept> result = new ArrayList<>();
        // 递归，简单粗暴
        this.listDeptsByParentIdFromCache(result, parentId,
                // 如果递归获取，则无限；否则，只递归 1 次
                recursive ? Integer.MAX_VALUE : 1,
                parentDeptCache);
        return result;
    }

    private void listDeptsByParentIdFromCache(List<Dept> result, Long parentId, int recursiveCount,
                                              Multimap<Long, Dept> parentDeptMap) {
        // 递归次数为 0，结束！
        if (recursiveCount == 0) {
            return;
        }
        // 获得子部门
        Collection<Dept> depts = parentDeptMap.get(parentId);
        if (CollUtil.isEmpty(depts)) {
            return;
        }
        result.addAll(depts);
        // 继续递归
        depts.forEach(dept -> listDeptsByParentIdFromCache(result, dept.getId(),
                recursiveCount - 1, parentDeptMap));
    }

    private void checkParentDeptEnable(Long id, Long parentId) {
        if (Objects.isNull(parentId) || DeptIdEnum.ROOT.getId().equals(parentId)) {
            return;
        }
        // 不能设置自己为父部门
        if (parentId.equals(id)) {
            throw ServiceExceptionUtil.get(DEPT_PARENT_ERROR);
        }
        // 父岗位不存在
        Dept dept = deptMapper.selectById(parentId);
        if (Objects.isNull(dept)) {
            throw ServiceExceptionUtil.get(DEPT_PARENT_NOT_EXITS);
        }
        // 父部门被禁用
        if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
            throw ServiceExceptionUtil.get(DEPT_NOT_ENABLE);
        }
        // 父部门不能是原来的子部门
        List<Dept> children = this.getDeptsByParentIdFromCache(id, true);
        if (children.stream().anyMatch(dept1 -> dept1.getId().equals(parentId))) {
            throw ServiceExceptionUtil.get(DEPT_PARENT_IS_CHILD);
        }
    }


    private void checkDeptExists(Long id) {
        if (Objects.nonNull(id) && Objects.isNull(deptMapper.selectById(id))) {
            throw ServiceExceptionUtil.get(DEPT_NOT_FOUND);
        }
    }

    private void checkDeptNameUnique(Long id, Long parentId, String name) {
        Dept dept = deptMapper.selectByParentIdAndName(parentId, name);
        if (Objects.nonNull(dept)) {
            // 如果 id 为空，说明不用比较是否为相同 id 的岗位
            if (Objects.isNull(id)) {
                throw ServiceExceptionUtil.get(DEPT_NAME_DUPLICATE);
            }
            if (!dept.getId().equals(id)) {
                throw ServiceExceptionUtil.get(DEPT_NAME_DUPLICATE);
            }
        }
    }
}
