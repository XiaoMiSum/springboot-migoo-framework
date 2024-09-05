package xyz.migoo.framework.infra.controller.stationletter.vo;

import lombok.Data;

import java.util.Set;

@Data
public class StationLetterUpdateReqVO {

    private Set<Long> ids;
}
