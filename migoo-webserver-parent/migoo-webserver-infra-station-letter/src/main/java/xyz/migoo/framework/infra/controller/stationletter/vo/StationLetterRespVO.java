package xyz.migoo.framework.infra.controller.stationletter.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

import static xyz.migoo.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static xyz.migoo.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
public class StationLetterRespVO {

    private Long id;

    private String title;

    private String content;

    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime create;

}
