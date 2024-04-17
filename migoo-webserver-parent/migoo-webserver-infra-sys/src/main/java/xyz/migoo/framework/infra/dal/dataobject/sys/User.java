package xyz.migoo.framework.infra.dal.dataobject.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;
import xyz.migoo.framework.mybatis.core.handler.JsonLongSetTypeHandler;
import xyz.migoo.framework.security.core.BaseUser;

import java.util.Set;

@TableName(value = "sys_user", autoResultMap = true)
@Getter
@Setter
public class User extends BaseDO<Long> implements BaseUser<Long> {

    private String username;

    private String password;

    private String name;

    private String phone;

    private String avatar;

    private Integer gender;

    private Integer status;

    private Long deptId;

    private String secretKey;

    @TableField(typeHandler = JsonLongSetTypeHandler.class)
    private Set<Long> postIds;

    private String email;
    
    private Integer requiredVerifyAuthenticator;

    private Integer bindAuthenticator;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }

}
