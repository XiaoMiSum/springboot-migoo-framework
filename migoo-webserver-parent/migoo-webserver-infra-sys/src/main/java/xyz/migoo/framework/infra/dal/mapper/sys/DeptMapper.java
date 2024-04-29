package xyz.migoo.framework.infra.dal.mapper.sys;

import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.infra.controller.sys.dept.vo.DeptQueryReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Dept;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DeptMapper extends BaseMapperX<Dept> {

    default List<Dept> selectList(DeptQueryReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<Dept>()
                .likeIfPresent(Dept::getName, reqVO.getName())
                .eqIfPresent(Dept::getStatus, reqVO.getStatus()));
    }

    default Dept selectByParentIdAndName(Long parentId, String name) {
        return selectOne(new LambdaQueryWrapperX<Dept>()
                .eq(Dept::getParentId, parentId)
                .eq(Dept::getName, name));
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(new LambdaQueryWrapperX<Dept>().eq(Dept::getParentId, parentId));
    }

    default boolean selectExistsByUpdateTimeAfter(LocalDateTime maxUpdateTime) {
        return selectOne(new LambdaQueryWrapperX<Dept>()
                .gt(Dept::getUpdateTime, maxUpdateTime).last("LIMIT 1")) != null;
    }
}
