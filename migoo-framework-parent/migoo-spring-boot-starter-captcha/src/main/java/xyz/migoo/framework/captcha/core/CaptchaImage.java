package xyz.migoo.framework.captcha.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiaomi
 * Created on 2021/11/21 16:50
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaImage {

    private String uuid;

    private String img;
}
