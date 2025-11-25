package xyz.migoo.framework.infra.controller.stationletter.vo;

import lombok.Data;
import xyz.migoo.framework.common.pojo.PageParam;

@Data
public class StationLetterPageReqVO extends PageParam {

    private String code;

    private String toUserId;

    private String toUserType;

    private String fromUserId;

    private String fromUserType;

    private Integer unread;

    public void setToUserId(Object toUserId) {
        this.toUserId = toUserId.toString();
    }
}
