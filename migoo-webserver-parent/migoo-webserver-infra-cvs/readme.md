# 基础组件

本项目为后台管理系统的云服务器管理模块后端，用以提供后台管理系统的云服务管理模块接口功能

- 云服务商账号配置
- 云服务器管理

# Urls

## 对外提供接口

接口url地址中的 base 为 框架 web组件 配置的接口前缀

### 云服务商账号

- 获取账号列表: GET {base}/developer/cvs/provider
- 获取账号详情: GET {base}/developer/cvs/provider/{id}
- 新增账号: POST {base}/developer/cvs/provider
- 修改账号: PUT {base}/developer/cvs/provider
- 删除账号: DELETE {base}/developer/cvs/provider/{id}
- 获取账号树: GET {base}/developer/cvs/provider/tree

### 云服务器

- 同步云服务器: POST {base}/developer/cvs/machine
- 更新云服务器: PUT {base}/developer/cvs/machine
- 删除云服务器: DELETE {base}/developer/cvs/machine/{id}
- 获取云服务器列表: GET {base}/developer/cvs/machine
- 开启云服务器: POST {base}/developer/cvs/machine/{id}/start
- 关闭云服务器: POST {base}/developer/cvs/machine/{id}/stop
- 重启云服务器: POST {base}/developer/cvs/machine/{id}/restart

# Spring Security

请将下列地址加入任意访问列表



