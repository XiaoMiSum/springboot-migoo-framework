package xyz.migoo.framework.infra.service.developer.sms;

import java.util.Map;

public interface SmsSendService {

    /**
     * 发送单条短信给用户
     *
     * @param mobile         手机号
     * @param userId         用户编号
     * @param userType       用户类型
     * @param templateCode   短信模板编号
     * @param templateParams 短信模板参数
     * @return 发送日志编号
     */
    Long doSendSms(String mobile, Long userId, Integer userType, String templateCode, Map<String, Object> templateParams);

    /**
     * 接收短信的接收结果
     *
     * @param channelCode 渠道编码
     * @param text        结果内容
     * @throws Throwable 处理失败时，抛出异常
     */
    void receiveSmsStatus(String channelCode, String text) throws Throwable;

}
