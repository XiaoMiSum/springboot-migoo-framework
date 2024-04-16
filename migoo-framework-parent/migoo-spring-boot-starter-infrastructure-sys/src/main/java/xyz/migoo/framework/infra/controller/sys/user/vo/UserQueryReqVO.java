package xyz.migoo.framework.infra.controller.sys.user.vo;

import lombok.Getter;
import lombok.Setter;
import xyz.migoo.framework.common.pojo.PageParam;
import xyz.migoo.framework.common.util.json.JsonUtils;

@Getter
@Setter
public class UserQueryReqVO extends PageParam {

    private String name;

    private String phone;

    private Integer status;

    private Long deptId;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
