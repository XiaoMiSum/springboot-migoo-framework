package xyz.migoo.framework.infra.controller.login.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaImageRespVO {

    private String uuid;

    private String img;

}
