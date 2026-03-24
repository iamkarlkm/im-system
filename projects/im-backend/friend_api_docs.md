# 好友关系管理 API

## 基础信息
- **基础URL**: `/api/friends`
- **认证方式**: JWT Bearer Token
- **CORS**: 允许跨域请求

## API 列表

### 1. 发送好友申请
- **端点**: `POST /api/friends/request`
- **认证**: 需要
- **请求体**:
  ```json
  {
    "friendId": 2
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "message": "好友申请已发送",
    "data": {
      "id": 1,
      "userId": 1,
      "user": {...},  // 申请用户信息
      "friendId": 2,
      "friend": {...}, // 目标用户信息
      "status": 0,     // 0=待确认
      "createTime": "2026-03-24T10:50:00"
    }
  }
  ```

### 2. 获取好友列表
- **端点**: `GET /api/friends`
- **认证**: 需要
- **响应**:
  ```json
  {
    "success": true,
    "message": "获取好友列表成功",
    "data": [
      {
        "id": 1,
        "userId": 1,
        "user": {...},
        "friendId": 2,
        "friend": {...},
        "status": 1,    // 1=已是好友
        "createTime": "2026-03-24T10:50:00"
      }
    ]
  }
  ```

### 3. 获取收到的好友申请
- **端点**: `GET /api/friends/requests/received`
- **认证**: 需要
- **响应**: 与好友列表类似，status=0

### 4. 获取发送的好友申请
- **端点**: `GET /api/friends/requests/sent`
- **认证**: 需要

### 5. 处理好友申请
- **端点**: `PUT /api/friends/request/{requestId}/handle?accept={true/false}`
- **认证**: 需要
- **参数**:
  - `requestId`: 申请ID
  - `accept`: true=同意，false=拒绝

### 6. 撤回好友申请
- **端点**: `DELETE /api/friends/request/{requestId}`
- **认证**: 需要

### 7. 删除好友
- **端点**: `DELETE /api/friends/{friendId}`
- **认证**: 需要

### 8. 搜索好友
- **端点**: `GET /api/friends/search?keyword={搜索词}`
- **认证**: 需要

### 9. 检查好友状态
- **端点**: `GET /api/friends/check/{friendId}`
- **认证**: 需要
- **响应**:
  ```json
  {
    "success": true,
    "message": "检查完成",
    "data": true  // true=已是好友，false=不是好友
  }
  ```

## 状态码说明
- `0`: 待确认（好友申请中）
- `1`: 已是好友
- `2`: 已拒绝（好友申请被拒绝）

## 错误处理
所有接口统一返回格式：
```json
{
  "success": false,
  "message": "错误信息",
  "data": null
}
```

常见错误信息：
- `"未认证用户"`
- `"好友申请已存在"`
- `"你们已经是好友"`
- `"不能添加自己为好友"`
- `"无权操作此申请"`
- `"用户不存在"`

## 使用示例

```bash
# 发送好友申请
curl -X POST http://localhost:8080/api/friends/request \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"friendId": 2}'

# 获取好友列表
curl -X GET http://localhost:8080/api/friends \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 处理好友申请
curl -X PUT "http://localhost:8080/api/friends/request/1/handle?accept=true" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```