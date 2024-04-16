package xyz.migoo.franework.infra.service.developer.sms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.core.KeyValue;
import xyz.migoo.framework.common.enums.CommonStatusEnum;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.sms.core.client.SmsClient;
import xyz.migoo.framework.sms.core.client.SmsClientFactory;
import xyz.migoo.framework.sms.core.client.dto.SmsReceiveRespDTO;
import xyz.migoo.framework.sms.core.property.SmsChannelProperties;
import xyz.migoo.franework.infra.convert.developer.sms.SmsChannelConvert;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsChannelDO;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsTemplateDO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.migoo.franework.infra.enums.ErrorCodeConstants.*;


@Service
public class SmsSendServiceImpl implements SmsSendService {

    @Resource
    private SmsChannelService smsChannelService;
    @Resource
    private SmsTemplateService smsTemplateService;
    @Resource
    private SmsLogService smsLogService;
    @Resource
    private SmsClientFactory smsClientFactory;

    @Override
    public Long doSendSms(String mobile, Long userId, Integer userType,
                          String templateCode, Map<String, Object> templateParams) {
        // 校验短信模板是否合法
        SmsTemplateDO template = validateSmsTemplate(templateCode);
        // 校验短信渠道是否合法
        SmsChannelDO smsChannel = validateSmsChannel(template.getChannelId());

        // 校验手机号码是否存在
        mobile = validateMobile(mobile);
        // 构建有序的模板参数。为什么放在这个位置，是提前保证模板参数的正确性，而不是到了插入发送日志
        List<KeyValue<String, Object>> newTemplateParams = buildTemplateParams(template, templateParams);

        // 创建发送日志。如果模板被禁用，则不发送短信，只记录日志
        Boolean isSend = CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus())
                && CommonStatusEnum.ENABLE.getStatus().equals(smsChannel.getStatus());
        String content = smsTemplateService.formatSmsTemplateContent(template.getContent(), templateParams);
        Long sendLogId = smsLogService.createSmsLog(mobile, userId, userType, isSend, template, content, templateParams);

        // 发送 MQ 消息，异步执行发送短信
        if (isSend) {
            SmsChannelProperties properties = SmsChannelConvert.INSTANCE.convert01(smsChannel);
            smsClientFactory.createOrUpdateSmsClient(properties);
            smsClientFactory.getSmsClient(smsChannel.getCode()).sendSms(sendLogId, mobile, content, newTemplateParams);
        }
        return sendLogId;
    }

    @VisibleForTesting
    SmsChannelDO validateSmsChannel(Long channelId) {
        // 获得短信模板。考虑到效率，从缓存中获取
        SmsChannelDO channelDO = smsChannelService.getSmsChannel(channelId);
        // 短信模板不存在
        if (channelDO == null) {
            throw ServiceExceptionUtil.get(SMS_CHANNEL_NOT_EXISTS);
        }
        return channelDO;
    }

    @VisibleForTesting
    SmsTemplateDO validateSmsTemplate(String templateCode) {
        // 获得短信模板。考虑到效率，从缓存中获取
        SmsTemplateDO template = smsTemplateService.getSmsTemplateByCodeFromCache(templateCode);
        // 短信模板不存在
        if (template == null) {
            throw ServiceExceptionUtil.get(SMS_SEND_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    /**
     * 将参数模板，处理成有序的 KeyValue 数组
     * <p>
     * 原因是，部分短信平台并不是使用 key 作为参数，而是数组下标，例如说腾讯云 https://cloud.tencent.com/document/product/382/39023
     *
     * @param template       短信模板
     * @param templateParams 原始参数
     * @return 处理后的参数
     */
    @VisibleForTesting
    List<KeyValue<String, Object>> buildTemplateParams(SmsTemplateDO template, Map<String, Object> templateParams) {
        return template.getParams().stream().map(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw ServiceExceptionUtil.get(SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS, key);
            }
            return new KeyValue<>(key, value);
        }).collect(Collectors.toList());
    }

    @VisibleForTesting
    public String validateMobile(String mobile) {
        if (StrUtil.isEmpty(mobile)) {
            throw ServiceExceptionUtil.get(SMS_SEND_MOBILE_NOT_EXISTS);
        }
        return mobile;
    }

    @Override
    public void receiveSmsStatus(String channelCode, String text) throws Throwable {
        // 获得渠道对应的 SmsClient 客户端
        SmsClient smsClient = smsClientFactory.getSmsClient(channelCode);
        Assert.notNull(smsClient, "短信客户端({}) 不存在", channelCode);
        // 解析内容
        List<SmsReceiveRespDTO> receiveResults = smsClient.parseSmsReceiveStatus(text);
        if (CollUtil.isEmpty(receiveResults)) {
            return;
        }
        // 更新短信日志的接收结果. 因为量一般不大，所以先使用 for 循环更新
        receiveResults.forEach(result -> smsLogService.updateSmsReceiveResult(result.getLogId(),
                result.getSuccess(), result.getReceiveTime(), result.getErrorCode(), result.getErrorMsg()));
    }

}
