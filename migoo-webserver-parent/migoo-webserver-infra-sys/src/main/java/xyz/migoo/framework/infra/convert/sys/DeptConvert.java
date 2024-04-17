package xyz.migoo.framework.infra.convert.sys;

import xyz.migoo.framework.infra.controller.sys.dept.vo.DeptAddReqVO;
import xyz.migoo.framework.infra.controller.sys.dept.vo.DeptRespVO;
import xyz.migoo.framework.infra.controller.sys.dept.vo.DeptSimpleRespVO;
import xyz.migoo.framework.infra.controller.sys.dept.vo.DeptUpdateReqVO;
import xyz.migoo.framework.infra.dal.dataobject.sys.Dept;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DeptConvert {

    DeptConvert INSTANCE = Mappers.getMapper(DeptConvert.class);

    Dept convert(DeptAddReqVO updateReq);

    Dept convert(DeptUpdateReqVO updateReq);

    DeptRespVO convert(Dept list);

    List<DeptRespVO> convert(List<Dept> list);

    List<DeptSimpleRespVO> convert0(List<Dept> list);
}
