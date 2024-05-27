package xyz.migoo.framework.sms.core.client.bark;

import cn.hutool.http.HttpUtil;
import com.google.common.collect.Lists;
import xyz.migoo.framework.common.core.KeyValue;
import xyz.migoo.framework.common.util.collection.MapUtils;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.sms.core.client.AbstractSmsClient;
import xyz.migoo.framework.sms.core.client.dto.SmsReceiveRespDTO;
import xyz.migoo.framework.sms.core.client.dto.SmsSendRespDTO;
import xyz.migoo.framework.sms.core.client.dto.SmsTemplateRespDTO;
import xyz.migoo.framework.sms.core.property.SmsChannelProperties;

import java.util.List;
import java.util.Map;

/**
 * @author xiaomi
 * Created at 2023/5/31 15:38
 */
public class BarkClient extends AbstractSmsClient {

    public BarkClient(SmsChannelProperties properties) {
        super(properties);
    }

    @Override
    protected void doInit() {

    }

    /**
     * @param sendLogId      发送日志id
     * @param mobile         接收方手机号（此处为BarkApp中的设备编码 kpi*****）
     * @param apiTemplateId  短信服务商中添加的模板编号（此处为发送的消息内容）
     * @param templateParams 短信模板中的参数
     * @return 发送结果
     * @throws Throwable 发送失败
     */
    @Override
    public SmsSendRespDTO sendSms(Long sendLogId, String mobile, String apiTemplateId, List<KeyValue<String, Object>> templateParams) {
        // 构建请求
        Map<String, Object> params = MapUtils.convertMap(templateParams);
        params.put("body", apiTemplateId);
        params.put("device_key", mobile);
        // 执行请求
        int status = HttpUtil.createPost("https://api.day.app/push")
                .body(JsonUtils.toJsonString(params))
                .header("Content-Type", "application/json; charset=utf-8")
                .execute().getStatus();
        // 解析结果
        return new SmsSendRespDTO().setSuccess(status == 200).setApiCode(String.valueOf(status))
                .setApiMsg(status == 200 ? "SUCCESS" : ("ERROR:" + status));

    }

    @Override
    public List<SmsReceiveRespDTO> parseSmsReceiveStatus(String text) {
        return Lists.newArrayList();
    }

    @Override
    public SmsTemplateRespDTO getSmsTemplate(String apiTemplateId) {
        return new SmsTemplateRespDTO();
    }
}
