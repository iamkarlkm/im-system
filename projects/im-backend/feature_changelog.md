# 功能更新日志 (Feature Changelog)

## [v1.1.2] - 2026-03-20

### ✨ 新增功能

#### 用户在线状态API（Online Status）
- **状态**: ✅ 已完成
- **描述**: 查询用户在线状态
- **涉及文件**:
  - `service/MessageService.java` - 新增 `registerSession()`, `removeSession()`, `getSession()`, `isUserOnline()` 方法
  - `controller/MessageController.java` - 预留在线状态查询接口
- **API**: 已实现WebSocket会话管理
- **逻辑**:
  - WebSocket连接时注册会话
  - 断开时移除会话
  - 可查询用户是否在线

## [v1.1.1] - 2026-03-20

### ✨ 新增功能

#### 消息已读回执（Read Receipt）
- **状态**: ✅ 已完成
- **描述**: 标记消息为已读状态
- **涉及文件**:
  - `repository/MessageRepository.java` - 新增 `markAsRead()` 方法
  - `service/MessageService.java` - 新增 `markAsRead()` 方法
  - `controller/MessageController.java` - 新增 PUT /api/messages/read/{senderId} 接口
- **API**: `PUT /api/messages/read/{senderId}`
- **逻辑**:
  - 接收者可以标记与发送者的未读消息为已读
  - 状态变更：status=1(未读) → status=2(已读)
  - 仅影响接收者的视角

#### 未读消息计数API（Unread Count）
- **状态**: ✅ 已完成
- **描述**: 获取各会话的未读消息数量
- **涉及文件**:
  - `repository/MessageRepository.java` - 新增 `countUnreadMessages()` 方法
  - `service/MessageService.java` - 新增 `getUnreadCount()` 方法
  - `controller/MessageController.java` - 新增 GET /api/messages/unread/count 接口
- **API**: `GET /api/messages/unread/count`
- **逻辑**:
  - 统计用户所有会话的未读消息总数
  - 只统计未读且未删除的消息

## [v1.1.0] - 2026-03-19

### ✨ 新增功能

#### 消息撤回功能（Message Recall）
- **状态**: ✅ 已完成
- **分支**: feature/message-recall
- **描述**: 支持用户撤回已发送的消息（2分钟内可撤回）
- **涉及文件**:
  - `dto/MessageRecallRequest.java` - 撤回请求DTO ✨ 新增
  - `service/MessageService.java` - 增强 `recallMessage()` 方法（增加时间限制、权限校验、WebSocket通知）
  - `service/WebSocketService.java` - 新增 `notifyMessageRecalled()` 和 `notifyGroupMessageRecalled()` 方法
  - `controller/MessageController.java` - 增强撤回API返回信息
- **API**: `DELETE /api/messages/{messageId}`
- **逻辑**:
  - 发送者可在2分钟内撤回自己的消息
  - 校验：权限检查、时间限制（2分钟）、重复撤回检查
  - 撤回后消息状态变为3（已撤回），前端显示"消息已撤回"
  - 通过WebSocket通知接收者消息被撤回
  - 支持群聊消息撤回通知
- **WebSocket通知类型**: `MESSAGE_RECALLED`, `GROUP_MESSAGE_RECALLED`

---
*最后更新: 2026-03-20*
*格式说明: ✅已完成 | 🔄开发中 | ⏳待开发 | ❌已取消 | ⚠️待人工解决*
