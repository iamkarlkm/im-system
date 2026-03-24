package com.im.backend.controller;

import com.im.backend.dto.ApiResponse;
import com.im.backend.dto.MessageDTO;
import com.im.backend.dto.SendMessageRequest;
import com.im.backend.model.User;
import com.im.backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 消息控制器 - REST API
 * 提供消息发送、查询历史、未读数统计、消息撤回/删除等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user.getId();
        }
        throw new RuntimeException("用户未登录");
    }

    /**
     * 发送消息（HTTP API，作为WebSocket的补充）
     * POST /api/messages/send
     */
    @PostMapping("/send")
    public ApiResponse<MessageDTO> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        Long senderId = getCurrentUserId();
        MessageDTO message = messageService.sendMessage(senderId, request);
        return ApiResponse.success("消息发送成功", message);
    }

    /**
     * 获取与某人的聊天记录
     * GET /api/messages/chat/{friendId}?page=0&size=20
     */
    @GetMapping("/chat/{friendId}")
    public ApiResponse<List<MessageDTO>> getChatHistory(
            @PathVariable Long friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        List<MessageDTO> messages = messageService.getChatHistory(userId, friendId, page, size);
        return ApiResponse.success(messages);
    }

    /**
     * 获取群聊消息记录
     * GET /api/messages/group/{groupId}?page=0&size=20
     */
    @GetMapping("/group/{groupId}")
    public ApiResponse<List<MessageDTO>> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<MessageDTO> messages = messageService.getGroupMessages(groupId, page, size);
        return ApiResponse.success(messages);
    }

    /**
     * 获取最近会话列表（包含最后一条消息）
     * GET /api/messages/conversations
     */
    @GetMapping("/conversations")
    public ApiResponse<List<MessageDTO>> getRecentConversations(
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = getCurrentUserId();
        List<MessageDTO> conversations = messageService.getRecentConversations(userId, limit);
        return ApiResponse.success(conversations);
    }

    /**
     * 获取未读消息总数
     * GET /api/messages/unread/count
     */
    @GetMapping("/unread/count")
    public ApiResponse<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        Long count = messageService.getUnreadCount(userId);
        return ApiResponse.success(count);
    }

    /**
     * 标记与某人的消息为已读
     * PUT /api/messages/read/{senderId}
     */
    @PutMapping("/read/{senderId}")
    public ApiResponse<Void> markAsRead(@PathVariable Long senderId) {
        Long userId = getCurrentUserId();
        messageService.markAsRead(userId, senderId);
        return ApiResponse.success(null);
    }

    /**
     * 撤回消息（2分钟内可撤回）
     * DELETE /api/messages/{messageId}
     */
    @DeleteMapping("/{messageId}")
    public ApiResponse<Void> recallMessage(@PathVariable Long messageId) {
        Long userId = getCurrentUserId();
        boolean success = messageService.recallMessage(userId, messageId);
        if (success) {
            return ApiResponse.success("消息已撤回", null);
        }
        return ApiResponse.error("撤回失败");
    }

    /**
     * 删除消息（软删除）
     * DELETE /api/messages/{messageId}/soft
     */
    @DeleteMapping("/{messageId}/soft")
    public ApiResponse<Void> deleteMessage(@PathVariable Long messageId) {
        Long userId = getCurrentUserId();
        messageService.deleteMessage(userId, messageId);
        return ApiResponse.success(null);
    }
}
