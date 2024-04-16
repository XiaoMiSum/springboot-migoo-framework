package xyz.migoo.framework.infra.service.sys.dept;

import xyz.migoo.framework.infra.controller.sys.dept.vo.DeptQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Dept;
import cn.hutool.core.collection.CollUtil;
import xyz.migoo.framework.common.util.collection.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface DeptService {

    void initLocalCache();

    /**
     * 获得指定编号的部门 Map
     *
     * @param ids 部门编号数组
     * @return 部门 Map
     */
    default Map<Long, Dept> getDeptMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollectionUtils.convertMap(getList(ids), Dept::getId);
    }

    List<Dept> getList(Collection<Long> ids);

    List<Dept> getList(DeptQueryReqVO req);

    void add(Dept dept);

    void update(Dept dept);

    void verify(Long id, Long parentId, String name);

    Dept get(Long id);

    void remove(Long id);
}
