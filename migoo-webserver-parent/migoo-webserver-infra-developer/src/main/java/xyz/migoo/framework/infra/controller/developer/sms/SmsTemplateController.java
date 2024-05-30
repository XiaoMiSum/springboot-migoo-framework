package xyz.migoo.framework.infra.controller.developer.sms;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.pojo.SimpleData;
import xyz.migoo.framework.infra.controller.developer.sms.vo.template.*;
import xyz.migoo.framework.infra.convert.developer.sms.SmsChannelConvert;
import xyz.migoo.framework.infra.convert.developer.sms.SmsTemplateConvert;
import xyz.migoo.framework.infra.dal.dataobject.developer.sms.SmsTemplateDO;
import xyz.migoo.framework.infra.service.developer.sms.SmsSendService;
import xyz.migoo.framework.infra.service.developer.sms.SmsTemplateService;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/developer/sms/template")
public class SmsTemplateController {

    @Resource
    private SmsTemplateService smsTemplateService;
    @Resource
    private SmsSendService smsSendService;

    @PostMapping
    @PreAuthorize("@ss.hasPermission('developer:sms:template:add')")
    public Result<Long> createSmsTemplate(@Valid @RequestBody SmsTemplateCreateReqVO createReqVO) {
        return Result.getSuccessful(smsTemplateService.createSmsTemplate(createReqVO));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:sms:template:update')")
    public Result<Boolean> updateSmsTemplate(@Valid @RequestBody SmsTemplateUpdateReqVO updateReqVO) {
        smsTemplateService.updateSmsTemplate(updateReqVO);
        return Result.getSuccessful(true);
    }

    @DeleteMapping
    @PreAuthorize("@ss.hasPermission('developer:sms:template:remove')")
    public Result<Boolean> deleteSmsTemplate(@RequestParam("id") Long id) {
        smsTemplateService.deleteSmsTemplate(id);
        return Result.getSuccessful(true);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:sms:template:query')")
    public Result<SmsTemplateRespVO> getSmsTemplate(@PathVariable("id") Long id) {
        SmsTemplateDO smsTemplate = smsTemplateService.getSmsTemplate(id);
        return Result.getSuccessful(SmsTemplateConvert.INSTANCE.convert(smsTemplate));
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:sms:template:query')")
    public Result<PageResult<SmsTemplateRespVO>> getSmsTemplatePage(@Valid SmsTemplatePageReqVO pageVO) {
        PageResult<SmsTemplateDO> pageResult = smsTemplateService.getSmsTemplatePage(pageVO);
        return Result.getSuccessful(SmsTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    @PostMapping("/send")
    @PreAuthorize("@ss.hasPermission('developer:sms:template:send:sms')")
    public Result<Long> sendSms(@Valid @RequestBody SmsTemplateSendReqVO sendReqVO) {
        return Result.getSuccessful(smsSendService.doSendSms(sendReqVO.getMobile(), null, null,
                sendReqVO.getTemplateCode(), sendReqVO.getTemplateParams()));
    }

    @GetMapping("/simple")
    public Result<List<SimpleData>> getSimpleSmsChannelList() {
        List<SmsTemplateDO> list = smsTemplateService.getSmsTemplates();
        // 排序后，返回给前端
        list.sort(Comparator.comparing(SmsTemplateDO::getId));
        return Result.getSuccessful(SmsChannelConvert.INSTANCE.convertList04(list));
    }

}
