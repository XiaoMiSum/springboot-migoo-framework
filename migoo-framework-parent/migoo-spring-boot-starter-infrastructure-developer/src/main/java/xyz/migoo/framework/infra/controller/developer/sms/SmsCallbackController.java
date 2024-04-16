package xyz.migoo.framework.infra.controller.developer.sms;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.service.developer.sms.SmsSendService;

@RestController
@RequestMapping("/developer/sms/callback")
public class SmsCallbackController {

    @Resource
    private SmsSendService smsSendService;

    @PostMapping("/{channel_code}")
    @PermitAll
    // todo  "阿里云短信的回调 参见 https://help.aliyun.com/document_detail/120998.html 文档")
    public Result<Boolean> receiveAliYunSmsStatus(@PathVariable("channel_code") String channelCode,
                                                  HttpServletRequest request) throws Throwable {
        /*
        String text = ServletUtils.getBody(request);
        smsSendService.receiveSmsStatus(SmsChannelEnum.ALIYUN.getCode(), text);
         */
        return Result.getSuccessful(true);
    }

    @PostMapping("/tencent")
    @PermitAll
    // "腾讯云短信的回调", description = "参见 https://cloud.tencent.com/document/product/382/52077 文档")
    public Result<Boolean> receiveTencentSmsStatus(HttpServletRequest request) throws Throwable {
        /*
        String text = ServletUtils.getBody(request);
         */
        return Result.getSuccessful(true);
    }

}
