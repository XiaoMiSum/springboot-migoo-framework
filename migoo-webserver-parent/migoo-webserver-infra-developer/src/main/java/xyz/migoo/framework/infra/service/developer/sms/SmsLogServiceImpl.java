package xyz.migoo.framework.infra.service.developer.sms;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.developer.sms.vo.log.SmsLogPageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsLogDO;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsTemplateDO;
import xyz.migoo.framework.infra.dal.mapper.developer.sms.SmsLogMapper;
import xyz.migoo.framework.infra.enums.sms.SmsReceiveStatusEnum;
import xyz.migoo.framework.infra.enums.sms.SmsSendStatusEnum;

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
        SmsLogDO logDO = new SmsLogDO();
        // 根据是否要发送，设置状态
        logDO.setSendStatus(Objects.equals(isSend, true) ? SmsSendStatusEnum.INIT.getStatus()
                : SmsSendStatusEnum.IGNORE.getStatus());
        // 设置手机相关字段
        logDO.setMobile(mobile).setUserId(userId).setUserType(userType);
        // 设置模板相关字段
        logDO.setTemplateId(template.getId()).setTemplateCode(template.getCode());
        logDO.setTemplateContent(templateContent).setTemplateParams(templateParams)
                .setApiTemplateId(template.getApiTemplateId());
        // 设置渠道相关字段
        logDO.setChannelId(template.getChannelId()).setChannelCode(template.getChannelCode());
        // 设置接收相关字段
        logDO.setReceiveStatus(SmsReceiveStatusEnum.INIT.getStatus());
        smsLogMapper.insert(logDO);
        return logDO.getId();
    }

    @Override
    public void updateSmsSendResult(Long id, Integer sendCode, String sendMsg,
                                    String apiSendCode, String apiSendMsg,
                                    String apiRequestId, String apiSerialNo) {
        SmsSendStatusEnum sendStatus = Result.isSuccessful(sendCode) ?
                SmsSendStatusEnum.SUCCESS : SmsSendStatusEnum.FAILURE;
        smsLogMapper.updateById((SmsLogDO) new SmsLogDO().setSendStatus(sendStatus.getStatus())
                .setSendTime(LocalDateTime.now()).setSendCode(sendCode).setSendMsg(sendMsg)
                .setApiSendCode(apiSendCode).setApiSendMsg(apiSendMsg)
                .setApiRequestId(apiRequestId).setApiSerialNo(apiSerialNo).setId(id));
    }

    @Override
    public void updateSmsReceiveResult(Long id, Boolean success, LocalDateTime receiveTime,
                                       String apiReceiveCode, String apiReceiveMsg) {
        SmsReceiveStatusEnum receiveStatus = Objects.equals(success, true) ?
                SmsReceiveStatusEnum.SUCCESS : SmsReceiveStatusEnum.FAILURE;
        smsLogMapper.updateById((SmsLogDO) new SmsLogDO().setReceiveStatus(receiveStatus.getStatus())
                .setReceiveTime(receiveTime).setApiReceiveCode(apiReceiveCode).setApiReceiveMsg(apiReceiveMsg).setId(id));
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
