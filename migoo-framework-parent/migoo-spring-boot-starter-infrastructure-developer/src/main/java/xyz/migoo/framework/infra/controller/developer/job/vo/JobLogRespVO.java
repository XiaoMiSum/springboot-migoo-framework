package xyz.migoo.franework.infra.controller.developer.job.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JobLogRespVO extends JobLogBaseVO {

    private Long id;

    private LocalDateTime createTime;

}
