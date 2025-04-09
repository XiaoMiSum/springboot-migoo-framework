package xyz.migoo.framework.infra.dal.dataobject.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;
import xyz.migoo.framework.security.core.BaseUser;

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

    private Long postId;

    private String email;

    private Integer requiredVerifyAuthenticator;

    private Integer bindAuthenticator;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }

}
