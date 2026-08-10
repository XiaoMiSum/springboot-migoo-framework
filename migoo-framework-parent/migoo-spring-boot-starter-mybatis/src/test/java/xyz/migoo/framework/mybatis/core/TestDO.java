package xyz.migoo.framework.mybatis.core;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 测试用实体，供 WrapperX 系列测试注册 TableInfo 使用
 */
@TableName("t_user")
public class TestDO {

    private Long id;
    private String name;
    private Long roleId;
    private Integer age;
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
