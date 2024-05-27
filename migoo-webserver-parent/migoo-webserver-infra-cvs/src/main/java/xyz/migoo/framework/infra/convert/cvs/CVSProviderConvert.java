package xyz.migoo.framework.infra.convert.cvs;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSProviderAddReqVO;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSProviderPageRespVO;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSProviderUpdateReqVO;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSProviderDO;

@Mapper
public interface CVSProviderConvert {

    CVSProviderConvert INSTANCE = Mappers.getMapper(CVSProviderConvert.class);

    CVSProviderPageRespVO convert(CVSProviderDO bean);

    PageResult<CVSProviderPageRespVO> convert(PageResult<CVSProviderDO> page);

    CVSProviderDO convert(CVSProviderAddReqVO bean);

    CVSProviderDO convert(CVSProviderUpdateReqVO bean);
}
