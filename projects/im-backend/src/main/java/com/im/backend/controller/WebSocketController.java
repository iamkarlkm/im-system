package com.im.backend.controller;

import com.im.backend.dto.SendMessageRequest;
import com.im.backend.dto.WsMessageDTO;
import com.im.backend.service.MessageService;
import com.im.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * WebSocket 消息控制器
 * 处理 STOMP 协议的消息路由，并集成 MessageService 实现消息持久化
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketService webSocketService;
    private final MessageService messageService;

    /**
     * 用户连接成功
     * 客户端订阅 /app/online 时触发
     */
    @MessageMapping("/online")
    @SendToUser("/queue/online")
    public WsMessageDTO onOnline(
            SimpMessageHeaderAccessor headerAccessor,
            @Payload WsMessageDTO message) {
        String username = message.getFrom();
        String sessionId = headerAccessor.getSessionId();
        log.info("用户上线: {}, sessionId: {}", username, sessionId);
        webSocketService.userOnline(username, sessionId);

        WsMessageDTO response = new WsMessageDTO();
        response.setType("online_ack");
        response.setFrom("server");
        response.setContent(username + " 已上线");
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 发送单聊消息
     * 客户端发送到 /app/chat/private/{toUser}
     * 消息会被持久化到数据库，并通过 WebSocket 推送给接收方
     */
    @MessageMapping("/chat/private/{toUser}")
    public void sendPrivateMessage(
            @DestinationVariable String toUser,
            @Payload WsMessageDTO message,
            SimpMessageHeaderAccessor headerAccessor) {
        String fromUser = message.getFrom();
        log.info("私聊消息: {} -> {}", fromUser, toUser);

        // 1. 持久化消息到数据库
        try {
            SendMessageRequest request = new SendMessageRequest();
            request.setReceiverId(Long.parseLong(toUser));
            request.setContent(message.getContent());
            request.setMsgType(parseMsgType(message.getContentType()));
            request.setConversationType("SINGLE");
            request.setConversationId(Long.parseLong(toUser));

            // 获取发送者 userId（从 username 解析）
            Long senderId = webSocketService.getUserIdByUsername(fromUser);
            if (senderId != null) {
                messageService.sendMessage(senderId, request);
            }
        } catch (Exception e) {
            log.error("消息持久化失败: {}", e.getMessage(), e);
        }

        // 2. 构建 WebSocket 消息
        WsMessageDTO msgToSend = new WsMessageDTO();
        msgToSend.setType("chat");
        msgToSend.setFrom(fromUser);
        msgToSend.setTo(toUser);
        msgToSend.setContent(message.getContent());
        msgToSend.setTimestamp(System.currentTimeMillis());
        msgToSend.setMsgId(message.getMsgId());
        msgToSend.setContentType(message.getContentType());

        // 3. 通过 WebSocket 推送给接收方
        messagingTemplate.convertAndSendToUser(toUser, "/queue/msg", msgToSend);
    }

    /**
     * 发送群聊消息
     * 客户端发送到 /app/chat/group/{groupId}
     */
    @MessageMapping("/chat/group/{groupId}")
    @SendTo("/topic/group/{groupId}")
    public WsMessageDTO sendGroupMessage(
            @DestinationVariable String groupId,
            @Payload WsMessageDTO message) {
        String fromUser = message.getFrom();
        log.info("群聊消息: {} -> group {}", fromUser, groupId);

        // 持久化群聊消息
        try {
            SendMessageRequest request = new SendMessageRequest();
            request.setReceiverId(Long.parseLong(groupId));
            request.setContent(message.getContent());
            request.setMsgType(parseMsgType(message.getContentType()));
            request.setConversationType("GROUP");
            request.setConversationId(Long.parseLong(groupId));

            Long senderId = webSocketService.getUserIdByUsername(fromUser);
            if (senderId != null) {
                messageService.sendMessage(senderId, request);
            }
        } catch (Exception e) {
            log.error("群聊消息持久化失败: {}", e.getMessage(), e);
        }

        WsMessageDTO msgToSend = new WsMessageDTO();
        msgToSend.setType("group_chat");
        msgToSend.setFrom(fromUser);
        msgToSend.setTo(groupId);
        msgToSend.setContent(message.getContent());
        msgToSend.setTimestamp(System.currentTimeMillis());
        msgToSend.setMsgId(message.getMsgId());
        msgToSend.setContentType(message.getContentType());

        return msgToSend;
    }

    /**
     * 心跳检测
     * 客户端发送到 /app/ping
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public WsMessageDTO onPing(@Payload WsMessageDTO message) {
        WsMessageDTO pong = new WsMessageDTO();
        pong.setType("pong");
        pong.setFrom("server");
        pong.setContent("pong");
        pong.setTimestamp(System.currentTimeMillis());
        return pong;
    }

    /**
     * 获取当前在线用户列表
     */
    @MessageMapping("/online/list")
    @SendTo("/topic/online/list")
    public WsMessageDTO getOnlineList(@Payload WsMessageDTO message) {
        WsMessageDTO response = new WsMessageDTO();
        response.setType("online_list");
        response.setFrom("server");
        response.setContent(webSocketService.getOnlineUsersJson());
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 将 contentType 字符串转换为消息类型整数
     * text->1, image->2, audio->3, video->4, file->5
     */
    private int parseMsgType(String contentType) {
        if (contentType == null) return 1;
        switch (contentType.toLowerCase()) {
            case "image": return 2;
            case "audio": return 3;
            case "video": return 4;
            case "file": return 5;
            default: return 1;
        }
    }
}
