package xyz.migoo.framework.infra.job;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.stereotype.Component;
import xyz.migoo.framework.common.core.KeyValue;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.common.util.thread.VirtualThreadUtils;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSMachineDO;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsChannelDO;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsTemplateDO;
import xyz.migoo.framework.infra.service.cvs.CVSMachineServiceImpl;
import xyz.migoo.framework.infra.service.developer.sms.SmsChannelService;
import xyz.migoo.framework.infra.service.developer.sms.SmsLogService;
import xyz.migoo.framework.infra.service.developer.sms.SmsTemplateService;
import xyz.migoo.framework.quartz.core.handler.JobHandler;
import xyz.migoo.framework.sms.core.client.dto.SmsSendRespDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static xyz.migoo.framework.common.enums.NumberConstants.N_0;

@Component
public class CVSMachineAlarmJobHandler implements JobHandler {

    @Resource
    private SmsChannelService smsChannelService;
    @Resource
    private SmsTemplateService smsTemplateService;
    @Resource
    private SmsLogService smsLogService;
    @Resource
    private CVSMachineServiceImpl machineService;

    @Override
    public String execute(String param, Long jobLogId) throws Exception {
        CloudServerRequestVO request = JsonUtils.parseObject(param, CloudServerRequestVO.class);
        SmsTemplateDO template = smsTemplateService.getSmsTemplateByCode(request.getTemplate());
        SmsChannelDO channel = smsChannelService.getSmsChannel(template.getChannelId());
        VirtualThreadUtils.submit(() -> {
            Map<String, List<CVSMachineDO>> maps = machineService.getList().stream()
                    .collect(Collectors.groupingBy(CVSMachineDO::getAccount));
            maps.forEach((key, value) -> {
                AtomicInteger total = new AtomicInteger(0);
                AtomicInteger amount = new AtomicInteger(0);
                value.forEach(item -> {
                    long day = DateUtil.between(DateUtil.date(), DateUtil.parseDate(item.getExpiredTime()), DateUnit.DAY);
                    if (day < request.getDays()) {
                        total.getAndIncrement();
                        amount.addAndGet(item.getPrice().intValue());
                    }
                });
                if (total.get() > N_0) {
                    List<KeyValue<String, Object>> keyValues = Lists.newArrayList();
                    Map<String, Object> params = Maps.newHashMap();
                    params.put("account", key);
                    params.put("total", total.get());
                    params.put("amount", amount.get());
                    String content = smsTemplateService.formatSmsTemplateContent(template.getContent(), params);
                    keyValues.add(new KeyValue<>("group", "云服务器"));
                    keyValues.add(new KeyValue<>("title", "续费提醒"));
                    keyValues.add(new KeyValue<>("icon", "https://day.app/assets/images/avatar.jpg"));
                    request.getDevices().forEach(device -> {
                        try {
                            Long sendLogId = smsLogService.createSmsLog(device, null, null, true, template, content, params);
                            SmsSendRespDTO result = smsChannelService.getSmsClient(channel.getId()).sendSms(sendLogId, device, content, keyValues);
                            smsLogService.updateSmsSendResult(sendLogId, result.getSuccess(), result.getApiCode(), result.getApiMsg(), result.getApiRequestId(), result.getSerialNo());
                        } catch (Throwable ignored) {

                        }
                    });
                }
            });
        });
        return "success";
    }

    @Data
    public static class CloudServerRequestVO {

        private List<String> devices;

        private Integer days;

        private String template;
    }
}
