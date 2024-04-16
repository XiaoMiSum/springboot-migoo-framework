package xyz.migoo.franework.infra.controller.developer.sms;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.franework.infra.controller.developer.sms.vo.channel.*;
import xyz.migoo.franework.infra.convert.developer.sms.SmsChannelConvert;
import xyz.migoo.franework.infra.dal.dataobject.developer.sms.SmsChannelDO;
import xyz.migoo.franework.infra.service.developer.sms.SmsChannelService;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/developer/sms/channel")
public class SmsChannelController {

    @Resource
    private SmsChannelService smsChannelService;

    @PostMapping
    @PreAuthorize("@ss.hasPermission('developer:sms:channel:add')")
    public Result<Long> createSmsChannel(@Valid @RequestBody SmsChannelCreateReqVO createReqVO) {
        return Result.getSuccessful(smsChannelService.createSmsChannel(createReqVO));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:sms:channel:update')")
    public Result<Boolean> updateSmsChannel(@Valid @RequestBody SmsChannelUpdateReqVO updateReqVO) {
        smsChannelService.updateSmsChannel(updateReqVO);
        return Result.getSuccessful(true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:sms:channel:remove')")
    public Result<Boolean> deleteSmsChannel(@PathVariable("id") Long id) {
        smsChannelService.deleteSmsChannel(id);
        return Result.getSuccessful(true);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:sms:channel:query')")
    public Result<SmsChannelRespVO> getSmsChannel(@PathVariable("id") Long id) {
        SmsChannelDO smsChannel = smsChannelService.getSmsChannel(id);
        return Result.getSuccessful(SmsChannelConvert.INSTANCE.convert(smsChannel));
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:sms:channel:query')")
    public Result<PageResult<SmsChannelRespVO>> getSmsChannelPage(@Valid SmsChannelPageReqVO pageVO) {
        PageResult<SmsChannelDO> pageResult = smsChannelService.getSmsChannelPage(pageVO);
        return Result.getSuccessful(SmsChannelConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/simple")
    public Result<List<SmsChannelSimpleRespVO>> getSimpleSmsChannelList() {
        List<SmsChannelDO> list = smsChannelService.getSmsChannelList();
        // 排序后，返回给前端
        list.sort(Comparator.comparing(SmsChannelDO::getId));
        return Result.getSuccessful(SmsChannelConvert.INSTANCE.convertList03(list));
    }

}
