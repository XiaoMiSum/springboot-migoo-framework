# 基础组件

本项目为后台管理系统的文件管理模块后端，用以提供后台管理系统的文件管理模块接口功能

- 文件上传配置
- 文件管理

# Urls

## 对外提供接口

接口url地址中的 base 为 框架 web组件 配置的接口前缀

### 文件上传配置

- 获取配置列表: GET {base}/developer/file/config
- 新增配置: POST {base}/developer/file/config
- 修改配置: PUT {base}/developer/file/config
- 修改为默认配置: PUT {base}/developer/file/master
- 获取配置详情: GET {base}/developer/file/config/{id}
- 删除配置: DELETE {base}/developer/file/config/{id}
- 测试上传: GET {base}/developer/file/config/test

### 文件管理

- 上传文件: POST {base}/developer/file/upload
- 生成文件预签名地址信息: GET {base}/developer/file/presigned-url
- 创建文件: POST {base}/developer/file/create
- 删除文件: DELETE {base}/developer/file/{id}
- 获取文件: GET {base}/developer/file/{configId}/**
- 获取文件列表: GET {base}/developer/file/

# Spring Security

请将下列地址加入任意访问列表

- {base}/d/f/*/*

