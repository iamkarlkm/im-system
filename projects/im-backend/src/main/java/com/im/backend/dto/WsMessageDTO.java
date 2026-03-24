package com.im.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息 DTO
 * 用于前后端 WebSocket 通信
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsMessageDTO {
    
    /**
     * 消息类型：
     * - chat: 私聊消息
     * - group_chat: 群聊消息
     * - online: 上线通知
     * - offline: 下线通知
     * - ping: 心跳
     * - pong: 心跳响应
     * - online_ack: 上线确认
     * - online_list: 在线用户列表
     * - error: 错误消息
     */
    private String type;
    
    /**
     * 发送者用户名
     */
    private String from;
    
    /**
     * 接收者用户名（私聊）或 群ID（群聊）
     */
    private String to;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息ID（客户端生成，用于去重）
     */
    private String msgId;
    
    /**
     * 消息时间戳（毫秒）
     */
    private Long timestamp;
    
    /**
     * 消息类型：text/image/file
     */
    private String contentType;
    
    /**
     * 扩展字段（JSON格式）
     */
    private String ext;
}
