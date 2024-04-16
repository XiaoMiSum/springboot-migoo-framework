package xyz.migoo.franework.infra.controller.developer.sms.vo.log;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SmsLogRespVO {

    private Long id;

    private Long channelId;

    private String channelCode;

    private Long templateId;

    private String templateCode;

    private String templateContent;

    private Map<String, Object> templateParams;

    private String apiTemplateId;

    private String mobile;

    private Long userId;

    private Integer userType;

    private Integer sendStatus;

    private LocalDateTime sendTime;

    private Integer sendCode;

    private String sendMsg;

    private String apiSendCode;

    private String apiSendMsg;

    private String apiRequestId;

    private String apiSerialNo;

    private Integer receiveStatus;

    private LocalDateTime receiveTime;

    private String apiReceiveCode;

    private String apiReceiveMsg;

    private LocalDateTime createTime;

}
