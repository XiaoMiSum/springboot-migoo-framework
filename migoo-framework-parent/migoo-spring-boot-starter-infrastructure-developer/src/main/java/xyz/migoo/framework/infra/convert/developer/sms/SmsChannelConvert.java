package xyz.migoo.franework.infra.convert.developer.sms;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.sms.core.property.SmsChannelProperties;
import xyz.migoo.franework.infra.controller.developer.CodeNameSimpleRespVO;
import xyz.migoo.franework.infra.controller.developer.sms.vo.channel.SmsChannelCreateReqVO;
import xyz.migoo.franework.infra.controller.developer.sms.vo.channel.SmsChannelRespVO;
import xyz.migoo.franework.infra.controller.developer.sms.vo.channel.SmsChannelSimpleRespVO;
import xyz.migoo.franework.infra.controller.developer.sms.vo.channel.SmsChannelUpdateReqVO;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsChannelDO;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsTemplateDO;

import java.util.List;

@Mapper
public interface SmsChannelConvert {

    SmsChannelConvert INSTANCE = Mappers.getMapper(SmsChannelConvert.class);

    SmsChannelDO convert(SmsChannelCreateReqVO bean);

    SmsChannelDO convert(SmsChannelUpdateReqVO bean);

    SmsChannelRespVO convert(SmsChannelDO bean);

    List<SmsChannelRespVO> convertList(List<SmsChannelDO> list);

    PageResult<SmsChannelRespVO> convertPage(PageResult<SmsChannelDO> page);

    SmsChannelProperties convert01(SmsChannelDO bean);

    List<SmsChannelProperties> convertList02(List<SmsChannelDO> list);

    List<SmsChannelSimpleRespVO> convertList03(List<SmsChannelDO> list);

    List<CodeNameSimpleRespVO> convertList04(List<SmsTemplateDO> list);

}
