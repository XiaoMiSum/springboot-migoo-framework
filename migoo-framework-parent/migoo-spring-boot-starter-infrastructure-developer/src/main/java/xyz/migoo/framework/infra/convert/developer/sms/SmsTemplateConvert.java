package xyz.migoo.franework.infra.convert.developer.sms;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.franework.infra.controller.developer.sms.vo.template.SmsTemplateCreateReqVO;
import xyz.migoo.franework.infra.controller.developer.sms.vo.template.SmsTemplateRespVO;
import xyz.migoo.franework.infra.controller.developer.sms.vo.template.SmsTemplateUpdateReqVO;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsTemplateDO;

import java.util.List;

@Mapper
public interface SmsTemplateConvert {

    SmsTemplateConvert INSTANCE = Mappers.getMapper(SmsTemplateConvert.class);

    SmsTemplateDO convert(SmsTemplateCreateReqVO bean);

    SmsTemplateDO convert(SmsTemplateUpdateReqVO bean);

    SmsTemplateRespVO convert(SmsTemplateDO bean);

    List<SmsTemplateRespVO> convertList(List<SmsTemplateDO> list);

    PageResult<SmsTemplateRespVO> convertPage(PageResult<SmsTemplateDO> page);

}
