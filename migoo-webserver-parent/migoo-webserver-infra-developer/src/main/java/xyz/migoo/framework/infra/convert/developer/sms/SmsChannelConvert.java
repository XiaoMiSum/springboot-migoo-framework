package xyz.migoo.framework.infra.convert.developer.sms;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.SimpleData;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelCreateReqVO;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelRespVO;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelSimpleRespVO;
import xyz.migoo.framework.infra.controller.developer.sms.vo.channel.SmsChannelUpdateReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsChannelDO;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsTemplateDO;
import xyz.migoo.framework.sms.core.property.SmsChannelProperties;

import java.util.List;

import static xyz.migoo.framework.common.enums.CommonStatusEnum.isDisabled;

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

    List<SimpleData> convertList04(List<SmsTemplateDO> list);

    default SimpleData convert(SmsTemplateDO bean) {
        return new SimpleData(bean.getCode(), bean.getName(), isDisabled(bean.getStatus()));
    }
}
