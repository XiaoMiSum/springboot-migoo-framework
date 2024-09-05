package xyz.migoo.framework.infra.controller.stationletter.vo;

import lombok.Data;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
public class StationLetterPageReqVO extends PageParam {

    private String code;

    private Long toUserId;

    private String toUserType;

    private Long fromUserId;

    private String fromUserType;

    private Integer unread;
}
