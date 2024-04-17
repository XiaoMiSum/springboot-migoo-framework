package xyz.migoo.framework.infra.convert.developer.sms;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.sms.vo.log.SmsLogRespVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsLogDO;

import java.util.List;

@Mapper
public interface SmsLogConvert {

    SmsLogConvert INSTANCE = Mappers.getMapper(SmsLogConvert.class);

    SmsLogRespVO convert(SmsLogDO bean);

    List<SmsLogRespVO> convertList(List<SmsLogDO> beans);

    PageResult<SmsLogRespVO> convertPage(PageResult<SmsLogDO> page);

}
