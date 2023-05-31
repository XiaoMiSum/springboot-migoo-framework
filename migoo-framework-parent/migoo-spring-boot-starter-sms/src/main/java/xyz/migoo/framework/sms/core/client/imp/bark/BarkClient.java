package xyz.migoo.framework.sms.core.client.imp.bark;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.google.common.collect.Lists;
import xyz.migoo.framework.common.core.KeyValue;
import xyz.migoo.framework.common.util.collection.MapUtils;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.sms.core.client.SmsResult;
import xyz.migoo.framework.sms.core.client.dto.SmsReceiveRespDTO;
import xyz.migoo.framework.sms.core.client.dto.SmsSendRespDTO;
import xyz.migoo.framework.sms.core.client.dto.SmsTemplateRespDTO;
import xyz.migoo.framework.sms.core.client.imp.AbstractSmsClient;
import xyz.migoo.framework.sms.core.property.SmsChannelProperties;

import java.util.List;
import java.util.Map;

/**
 * @author xiaomi
 * Created at 2023/5/31 15:38
 */
public class BarkClient extends AbstractSmsClient {

    public BarkClient(SmsChannelProperties properties) {
        super(properties, new BarkCodeMapping());
    }

    @Override
    protected void doInit() {

    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    protected SmsResult<SmsSendRespDTO> doSendSms(Long sendLogId, String mobile, String apiTemplateId, List<KeyValue<String, Object>> templateParams) throws Throwable {
        // 构建请求
        Map<String, Object> params = MapUtils.convertMap(templateParams);
        params.put("device_key", properties.getApiKey());
        // 执行请求
        int status = HttpUtil.createPost("https://api.day.app/push")
                .body(JsonUtils.toJsonString(params))
                .header("Content-Type", "application/json; charset=utf-8")
                .execute().getStatus();
        // 解析结果
        return SmsResult.build(String.valueOf(status), status == 200 ? "SUCCESS" : ("ERROR:" + status),
                null, new SmsSendRespDTO().setSerialNo(StrUtil.uuid()), codeMapping);

    }

    @Override
    protected List<SmsReceiveRespDTO> doParseSmsReceiveStatus(String text) throws Throwable {
        return Lists.newArrayList();
    }

    @Override
    protected SmsResult<SmsTemplateRespDTO> doGetSmsTemplate(String apiTemplateId) throws Throwable {
        return null;
    }
}
