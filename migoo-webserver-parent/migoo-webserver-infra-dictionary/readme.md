# 基础组件

本项目为后台管理系统的开发者模块字典管理后端，用以提供后台管理系统的开发者模块字典管理接口功能

# Urls

## 对外提供接口

接口url地址中的 base 为 框架 web组件 配置的接口前缀

### 字典管理

- 获取字典列表: GET {base}/developer/dictionary
- 新增字典: POST {base}/developer/dictionary
- 修改字典: PUT {base}/developer/dictionary
- 删除字典: DELETE {base}/developer/dictionary/{id}
- 获取字典下拉: GET {base}/developer/dictionary/simple

### 字典数据

- 获取字典数据: GET {base}/developer/dictionary/value
- 新增字典数据: POST {base}/developer/dictionary/value
- 修改字典数据: PUT {base}/developer/dictionary/value
- 删除字典数据: DELETE {base}/developer/dictionary/value/{id}
- 获取字典数据下拉: GET {base}/developer/dictionary/value/simple

# Spring Security

请将下列地址加入任意访问列表

