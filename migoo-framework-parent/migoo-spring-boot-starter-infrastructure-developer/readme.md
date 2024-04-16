# 基础组件

本项目为后台管理系统的开发者模块后端，用以提供后台管理系统的开发者模块接口功能

- 异常日志
- 定时任务
- 短信管理

# Urls

## 对外提供接口

接口url地址中的 base 为 框架 web组件 配置的接口前缀

### 异常日志

- 获取日志列表: GET {base}/developer/error-log
- 修改日志: PUT {base}/developer/error-log
- 获取日志详情: GET {base}/developer/error-log/{id}
- 删除日志: DELETE {base}/developer/error-log/{id}

### 定时任务

- 获取任务列表: GET {base}/developer/job
- 新增任务: POST {base}/developer/job
- 修改任务: PUT {base}/developer/job
- 启动一次任务: PUT {base}/developer/job/trigger
- 修改任务状态: PUT {base}/developer/job/{id}
- 获取任务信息: GET {base}/developer/job/{id}
- 删除任务: DELETE {base}/developer/job/{id}
- 获取任务下次执行时间: GET {base}/developer/job/get_next_times
- 获取任务日志列表: GET {base}/developer/job/log
- 获取任务日志详情: GET {base}/developer/job/log/{id}

### 短信管理

- 获取短信渠道列表: GET {base}/developer/sms/channel
- 新增短信渠道: POST {base}/developer/sms/channel
- 修改短信渠道: PUT {base}/developer/sms/channel
- 获取短信渠道详情: GET {base}/developer/sms/channel/{id}
- 删除短信渠道: DELETE {base}/developer/sms/channel/{id}
- 获取短信渠道下拉: GET {base}/developer/sms/channel/simple

- 获取短信模板列表: GET {base}/developer/sms/template
- 新增短信模板: POST {base}/developer/sms/template
- 修改短信模板: PUT {base}/developer/sms/template
- 获取短信模板详情: GET {base}/developer/sms/template/{id}
- 删除短信模板: DELETE {base}/developer/sms/template/{id}
- 获取短信模板下拉: GET {base}/developer/sms/template/simple
- 发送短信: GET {base}/developer/sms/template/send

- 获取短信日志列表: GET {base}/developer/sms/log
- 获取短信日志详情: GET {base}/developer/sms/log/{id}

- 阿里云渠道回调: POST {base}/developer/sms/callback/{CHANNEL_CODE}

# Spring Security

请将下列地址加入任意访问列表

- {base}/developer/sms/callback/**
