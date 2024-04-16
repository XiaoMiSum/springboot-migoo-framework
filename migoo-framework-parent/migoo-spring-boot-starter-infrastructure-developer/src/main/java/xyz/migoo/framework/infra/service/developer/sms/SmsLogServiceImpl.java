package xyz.migoo.franework.infra.service.developer.sms;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.franework.infra.controller.developer.sms.vo.log.SmsLogPageReqVO;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsLogDO;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsTemplateDO;
import xyz.migoo.franework.infra.dal.mapper.developer.sms.SmsLogMapper;
import xyz.migoo.franework.infra.enums.sms.SmsReceiveStatusEnum;
import xyz.migoo.franework.infra.enums.sms.SmsSendStatusEnum;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class SmsLogServiceImpl implements SmsLogService {

    @Resource
    private SmsLogMapper smsLogMapper;

    @Override
    public Long createSmsLog(String mobile, Long userId, Integer userType, Boolean isSend,
                             SmsTemplateDO template, String templateContent, Map<String, Object> templateParams) {
        SmsLogDO.SmsLogDOBuilder logBuilder = SmsLogDO.builder();
        // 根据是否要发送，设置状态
        logBuilder.sendStatus(Objects.equals(isSend, true) ? SmsSendStatusEnum.INIT.getStatus()
                : SmsSendStatusEnum.IGNORE.getStatus());
        // 设置手机相关字段
        logBuilder.mobile(mobile).userId(userId).userType(userType);
        // 设置模板相关字段
        logBuilder.templateId(template.getId()).templateCode(template.getCode());
        logBuilder.templateContent(templateContent).templateParams(templateParams)
                .apiTemplateId(template.getApiTemplateId());
        // 设置渠道相关字段
        logBuilder.channelId(template.getChannelId()).channelCode(template.getChannelCode());
        // 设置接收相关字段
        logBuilder.receiveStatus(SmsReceiveStatusEnum.INIT.getStatus());

        // 插入数据库
        SmsLogDO logDO = logBuilder.build();
        smsLogMapper.insert(logDO);
        return logDO.getId();
    }

    @Override
    public void updateSmsSendResult(Long id, Integer sendCode, String sendMsg,
                                    String apiSendCode, String apiSendMsg,
                                    String apiRequestId, String apiSerialNo) {
        SmsSendStatusEnum sendStatus = Result.isSuccessful(sendCode) ?
                SmsSendStatusEnum.SUCCESS : SmsSendStatusEnum.FAILURE;
        smsLogMapper.updateById((SmsLogDO) SmsLogDO.builder().sendStatus(sendStatus.getStatus())
                .sendTime(LocalDateTime.now()).sendCode(sendCode).sendMsg(sendMsg)
                .apiSendCode(apiSendCode).apiSendMsg(apiSendMsg)
                .apiRequestId(apiRequestId).apiSerialNo(apiSerialNo).build().setId(id));
    }

    @Override
    public void updateSmsReceiveResult(Long id, Boolean success, LocalDateTime receiveTime,
                                       String apiReceiveCode, String apiReceiveMsg) {
        SmsReceiveStatusEnum receiveStatus = Objects.equals(success, true) ?
                SmsReceiveStatusEnum.SUCCESS : SmsReceiveStatusEnum.FAILURE;
        smsLogMapper.updateById((SmsLogDO) SmsLogDO.builder().receiveStatus(receiveStatus.getStatus())
                .receiveTime(receiveTime).apiReceiveCode(apiReceiveCode).apiReceiveMsg(apiReceiveMsg).build().setId(id));
    }

    @Override
    public PageResult<SmsLogDO> getSmsLogPage(SmsLogPageReqVO pageReqVO) {
        return smsLogMapper.selectPage(pageReqVO);
    }

    @Override
    public SmsLogDO getData(Long id) {
        return smsLogMapper.selectById(id);
    }

}
