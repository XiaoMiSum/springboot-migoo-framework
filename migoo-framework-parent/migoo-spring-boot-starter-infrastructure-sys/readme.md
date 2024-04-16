# 基础组件

本项目为后台管理系统的系统管理模块后端，用以提供后台管理系统的基础接口功能

- 登录验证
- 用户管理
- 组织管理
- 岗位管理
- 角色管理
- 菜单管理

# Urls

## 对外提供接口

接口url地址中的 base 为 框架 web组件 配置的接口前缀

### 基础接口

- 登录接口: POST {base}/login
- 获取验证码接口: GET {base}/captcha
- 获取登录用户信息: GET {base}/user-info
- 获取登录用户菜单: GET {base}/user-menus
- 用户获取谷歌验证器信息: GET {base}/authenticator
- 用户绑定谷歌验证器: POST {base}/authenticator
- 用户修改密码: POST {base}/password

### 用户管理

- 获取用户列表: GET {base}/user
- 新增用户: POST {base}/user
- 修改用户: PUT {base}/user
- 获取用户信息: GET {base}/user/{id}
- 删除用户: DELETE {base}/user/{id}
- 获取用户下拉: GET {base}/user/simple
- 获取用户角色: GET {base}/user/{userId}/role
- 设置用户角色： POST {base}/user/role
- 修改用户密码: POST {base}/password
- 解绑用户谷歌验证器: POST {base}/user/{id}/authenticator

### 岗位管理

- 获取岗位列表: GET {base}/post
- 新增岗位: POST {base}/post
- 修改岗位: PUT {base}/post
- 获取岗位信息: GET {base}/post/{id}
- 删除岗位: DELETE {base}/post/{id}
- 获取岗位下拉: GET {base}/post/simple

### 部门管理

- 获取部门列表: GET {base}/dept
- 新增部门: POST {base}/dept
- 修改部门: PUT {base}/dept
- 获取部门信息: GET {base}/dept/{id}
- 删除部门: DELETE {base}/dept/{id}
- 获取部门下拉: GET {base}/dept/simple

### 菜单管理

- 获取菜单列表: GET {base}/menu
- 新增菜单: POST {base}/menu
- 修改菜单: PUT {base}/menu
- 获取菜单信息: GET {base}/menu/{id}
- 删除菜单: DELETE {base}/menu/{id}
- 获取菜单下拉: GET {base}/menu/simple

### 角色管理

- 获取角色列表: GET {base}/role
- 新增角色: POST {base}/role
- 修改角色: PUT {base}/role
- 修改角色状态: PUT {base}/role/status
- 获取角色信息: GET {base}/role/{id}
- 删除角色: DELETE {base}/role/{id}
- 获取角色下拉: GET {base}/role/simple
- 获取角色菜单: GET {base}/role/{roleId}/menu
- 设置角色菜单: POST {base}/role/{roleId}/menu

# Spring Security

请将下列地址加入任意访问列表

- {base}/login
- {base}/captcha