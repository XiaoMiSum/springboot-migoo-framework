package xyz.migoo.framework.infra.dal.dataobject.sys;

import xyz.migoo.framework.infra.dal.dataobject.BaseUser;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.mybatis.core.handler.JsonLongSetTypeHandler;

import java.util.Set;

@TableName(value = "sys_user", autoResultMap = true)
@Getter
@Setter
public class User extends BaseUser {

    private String phone;

    private Integer gender;

    private Long deptId;

    @TableField(typeHandler = JsonLongSetTypeHandler.class)
    private Set<Long> postIds;

    private String email;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
