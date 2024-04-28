package xyz.migoo.framework.infra.controller.area.vo;

import lombok.Data;

import java.util.List;

@Data
public class AreaNodeRespVO {

    private Integer id;

    private String name;

    /**
     * 子节点
     */
    private List<AreaNodeRespVO> children;

}
