package xyz.migoo.framework.sms.core.enums;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信渠道枚举
 *
 * @author xiaomi
 */
@Getter
@AllArgsConstructor
public enum SmsChannelEnum {

    BARK("BARK", "BARK"),
    ALI_CLOUD("ALI_CLOUD", "阿里云"),
    TENCENT("TENCENT", "腾讯云"),
    ;

    /**
     * 编码
     */
    private final String code;
    /**
     * 名字
     */
    private final String name;

    public static SmsChannelEnum getByCode(String code) {
        return ArrayUtil.firstMatch(o -> o.getCode().equalsIgnoreCase(code), values());
    }

}
