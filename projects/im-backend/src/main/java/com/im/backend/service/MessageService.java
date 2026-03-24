package com.im.backend.service;

import com.im.backend.dto.MessageDTO;
import com.im.backend.dto.SendMessageRequest;
import com.im.backend.model.Message;
import com.im.backend.model.User;
import com.im.backend.repository.FriendRepository;
import com.im.backend.repository.MessageRepository;
import com.im.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final WebSocketService webSocketService;
    
    // 存储用户WebSocket会话 {userId: session}
    private static final Map<Long, Object> userSessions = new HashMap<>();
    
    /**
     * 发送消息
     */
    @Transactional
    public MessageDTO sendMessage(Long senderId, SendMessageRequest request) {
        // 验证接收者存在
        userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("接收者不存在"));
        
        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .msgType(request.getMsgType() != null ? request.getMsgType() : 1)
                .conversationType(request.getConversationType() != null ? request.getConversationType() : "SINGLE")
                .conversationId(request.getConversationId() != null ? request.getConversationId() : request.getReceiverId())
                .content(request.getContent())
                .status(1)
                .isDeleted(0)
                .build();
        
        message = messageRepository.save(message);
        log.info("用户 {} 发送消息给 {}: {}", senderId, request.getReceiverId(), request.getContent());
        
        return toMessageDTO(message);
    }
    
    /**
     * 获取与某人的聊天记录
     */
    public List<MessageDTO> getChatHistory(Long userId, Long friendId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Message> messages = messageRepository.findChatHistory(userId, friendId, pageRequest);
        return messages.stream().map(this::toMessageDTO).collect(Collectors.toList());
    }
    
    /**
     * 获取群聊消息记录
     */
    public List<MessageDTO> getGroupMessages(Long groupId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Message> messages = messageRepository.findGroupMessages(groupId, pageRequest);
        return messages.stream().map(this::toMessageDTO).collect(Collectors.toList());
    }
    
    /**
     * 获取最近会话列表（包含最后一条消息）
     * 按最后消息时间倒序排列
     */
    public List<MessageDTO> getRecentConversations(Long userId, int limit) {
        // 获取用户的所有好友ID（通过好友关系表查询）
        List<Long> friendIds = friendRepository.findFriendsByUserId(userId).stream()
                .map(f -> f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId())
                .collect(Collectors.toList());
        
        if (friendIds.isEmpty()) {
            return List.of();
        }
        
        // 获取每个好友的最新一条消息
        List<Message> latestMessages = messageRepository.findLatestMessagesByFriends(userId, friendIds);
        
        // 转换为DTO并按时间倒序
        return latestMessages.stream()
                .sorted((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime()))
                .limit(limit)
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取未读消息数
     */
    public Long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }
    
    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long userId, Long senderId) {
        messageRepository.markAsRead(userId, senderId);
    }
    
    /**
     * 撤回消息（2分钟内可撤回）
     * @return true 撤回成功，false 撤回失败（超时或无权限）
     */
    @Transactional
    public boolean recallMessage(Long userId, Long messageId) {
        return messageRepository.findById(messageId)
                .map(m -> {
                    // 校验权限：只有发送者可以撤回
                    if (!m.getSenderId().equals(userId)) {
                        throw new RuntimeException("无权撤回此消息");
                    }
                    // 校验状态：已撤回的消息不能再撤回
                    if (m.getStatus() == 3) {
                        throw new RuntimeException("消息已被撤回");
                    }
                    // 校验时间：2分钟内可撤回（120秒）
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    java.time.LocalDateTime createTime = m.getCreateTime();
                    if (createTime.plusMinutes(2).isBefore(now)) {
                        throw new RuntimeException("消息已超过2分钟，无法撤回");
                    }
                    
                    m.setStatus(3);  // 撤回状态
                    messageRepository.save(m);
                    log.info("用户 {} 撤回了消息 {}（发送时间: {}）", userId, messageId, createTime);

                    // 通过WebSocket通知接收者
                    try {
                        webSocketService.notifyMessageRecalled(m.getReceiverId(), messageId, userId);
                        // 如果是群聊，通知所有群成员
                        if ("GROUP".equals(m.getConversationType())) {
                            webSocketService.notifyGroupMessageRecalled(m.getConversationId(), messageId, userId);
                        }
                    } catch (Exception e) {
                        log.warn("WebSocket撤回通知发送失败: {}", e.getMessage());
                    }

                    return true;
                })
                .orElseThrow(() -> new RuntimeException("消息不存在"));
    }
    
    /**
     * 删除消息
     */
    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        messageRepository.findById(messageId).ifPresent(m -> {
            if (m.getSenderId().equals(userId)) {
                m.setIsDeleted(1);
                messageRepository.save(m);
                log.info("用户 {} 删除了消息 {}", userId, messageId);
            }
        });
    }
    
    /**
     * 注册用户会话（WebSocket用）
     */
    public void registerSession(Long userId, Object session) {
        userSessions.put(userId, session);
        log.info("用户 {} WebSocket会话已注册", userId);
    }
    
    /**
     * 移除用户会话
     */
    public void removeSession(Long userId) {
        userSessions.remove(userId);
        log.info("用户 {} WebSocket会话已移除", userId);
    }
    
    /**
     * 获取用户会话
     */
    public Object getSession(Long userId) {
        return userSessions.get(userId);
    }
    
    /**
     * 判断用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return userSessions.containsKey(userId);
    }
    
    private MessageDTO toMessageDTO(Message message) {
        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .senderUsername(sender != null ? sender.getUsername() : null)
                .senderNickname(sender != null ? sender.getNickname() : null)
                .senderAvatar(sender != null ? sender.getAvatar() : null)
                .receiverId(message.getReceiverId())
                .msgType(message.getMsgType())
                .conversationType(message.getConversationType())
                .conversationId(message.getConversationId())
                .content(message.getContent())
                .status(message.getStatus())
                .createTime(message.getCreateTime())
                .build();
    }
}
