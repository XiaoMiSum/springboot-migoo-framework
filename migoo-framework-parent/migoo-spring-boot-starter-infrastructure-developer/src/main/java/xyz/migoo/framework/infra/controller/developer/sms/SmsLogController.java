package xyz.migoo.franework.infra.controller.developer.sms;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.franework.infra.controller.developer.sms.vo.log.SmsLogPageReqVO;
import xyz.migoo.franework.infra.controller.developer.sms.vo.log.SmsLogRespVO;
import xyz.migoo.franework.infra.convert.developer.sms.SmsLogConvert;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsLogDO;
import xyz.migoo.franework.infra.service.developer.sms.SmsLogService;

@RestController
@RequestMapping("/developer/sms/log")
@Validated
public class SmsLogController {

    @Resource
    private SmsLogService smsLogService;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:sms:log:query')")
    public Result<PageResult<SmsLogRespVO>> getSmsLogPage(@Valid SmsLogPageReqVO pageVO) {
        PageResult<SmsLogDO> pageResult = smsLogService.getSmsLogPage(pageVO);
        return Result.getSuccessful(SmsLogConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:sms:log:query')")
    public Result<SmsLogRespVO> getData(@PathVariable("id") Long id) {
        return Result.getSuccessful(SmsLogConvert.INSTANCE.convert(smsLogService.getData(id)));
    }

}
