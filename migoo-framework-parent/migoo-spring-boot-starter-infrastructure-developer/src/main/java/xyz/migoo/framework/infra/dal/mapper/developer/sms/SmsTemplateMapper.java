package xyz.migoo.franework.infra.dal.mapper.developer.sms;

import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;
import xyz.migoo.franework.infra.controller.developer.sms.vo.template.SmsTemplatePageReqVO;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsTemplateDO;

@Mapper
public interface SmsTemplateMapper extends BaseMapperX<SmsTemplateDO> {

    default SmsTemplateDO selectByCode(String code) {
        return selectOne(SmsTemplateDO::getCode, code);
    }

    default PageResult<SmsTemplateDO> selectPage(SmsTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SmsTemplateDO>()
                .eqIfPresent(SmsTemplateDO::getStatus, reqVO.getStatus())
                .likeIfPresent(SmsTemplateDO::getCode, reqVO.getCode())
                .likeIfPresent(SmsTemplateDO::getContent, reqVO.getContent())
                .likeIfPresent(SmsTemplateDO::getApiTemplateId, reqVO.getApiTemplateId())
                .eqIfPresent(SmsTemplateDO::getChannelId, reqVO.getChannelId())
                .betweenIfPresent(SmsTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SmsTemplateDO::getId));
    }

    default Long selectCountByChannelId(Long channelId) {
        return selectCount(SmsTemplateDO::getChannelId, channelId);
    }
}
