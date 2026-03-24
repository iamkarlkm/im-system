# 开发计划 (Development Plan)

## 项目概述
即时通讯系统后端 - Spring Boot 3.2.0 + MySQL + Redis + WebSocket

## 功能列表

| # | 功能 | 状态 | 优先级 | 备注 |
|---|------|------|--------|------|
| 1 | 消息撤回功能（Recall Message） | ✅ 已完成 | 高 | 支持撤回发送的消息（2分钟内），带WebSocket通知 |
| 2 | 消息已读回执（Read Receipt） | ✅ 已完成 | 高 | 标记消息为已读 |
| 3 | 用户搜索API（User Search） | 待开发 | 中 | 支持用户名/昵称搜索 |
| 4 | 用户资料更新API（Profile Update） | 待开发 | 中 | 更新头像、昵称、签名 |
| 5 | 群组管理API（Group CRUD） | 待开发 | 高 | 创建、加入、离开、删除群组 |
| 6 | 未读消息计数API（Unread Count） | ✅ 已完成 | 中 | 获取各会话未读数（`getUnreadCount()` 方法已实现） |
| 7 | 消息搜索API（Message Search） | 待开发 | 中 | 全文搜索聊天记录 |
| 8 | 用户在线状态API（Online Status） | ✅ 已完成 | 中 | 查询用户在线状态（`isUserOnline()` 方法已实现，WebSocket会话管理已就绪） |
| 9 | 消息免打扰设置（Do Not Disturb） | 待开发 | 低 | 免打扰时间段设置 |
| 10 | 登录设备管理（Device Management） | 待开发 | 中 | 查看和管理登录设备 |
| 11 | 消息草稿功能（Draft Message） | 待开发 | 低 | 保存未发送消息草稿 |
| 12 | 表情回复（Emoji Reaction） | 待开发 | 低 | 消息表情反应 |
| 13 | 阅后即焚（Burn After Reading） | 待开发 | 低 | 敏感消息阅后删除 |
| 14 | 文件上传服务（File Upload） | 待开发 | 中 | 媒体文件上传 |
| 15 | WebSocket心跳检测（Heartbeat） | 待开发 | 中 | 保持连接活跃 |

---
*最后更新: 2026-03-20*
