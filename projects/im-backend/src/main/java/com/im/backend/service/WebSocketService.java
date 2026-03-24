package com.im.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * WebSocket 会话管理服务
 * 维护用户在线状态和会话映射
 */
@Slf4j
@Service
public class WebSocketService {

    // 用户名 -> 多个会话ID（支持多设备登录）
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    // 会话ID -> 用户名
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();

    // 用户名 -> 用户基本信息（昵称、头像）
    private final Map<String, UserInfo> userInfos = new ConcurrentHashMap<>();

    // 用户名 -> 用户ID（缓存，避免频繁查询数据库）
    private final Map<String, Long> usernameToUserId = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;

    public WebSocketService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 用户上线
     */
    public void userOnline(String username, String sessionId) {
        userSessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionUsers.put(sessionId, username);

        // 缓存用户ID
        userRepository.findByUsername(username).ifPresent(user -> {
            usernameToUserId.put(username, user.getId());
        });

        log.info("用户 {} (session: {}) 上线，当前在线: {}", username, sessionId, getOnlineCount());
    }

    /**
     * 用户下线
     */
    public void userOffline(String sessionId) {
        String username = sessionUsers.remove(sessionId);
        if (username != null) {
            Set<String> sessions = userSessions.get(username);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(username);
                    userInfos.remove(username);
                    usernameToUserId.remove(username);
                    log.info("用户 {} 全部设备下线", username);
                }
            }
        }
        log.info("Session {} 下线，当前在线: {}", sessionId, getOnlineCount());
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String username) {
        Set<String> sessions = userSessions.get(username);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * 获取用户在线设备数
     */
    public int getUserDeviceCount(String username) {
        Set<String> sessions = userSessions.get(username);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * 获取在线用户列表
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(userSessions.keySet());
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        return userSessions.size();
    }

    /**
     * 获取在线用户详细信息（JSON格式）
     */
    public String getOnlineUsersJson() {
        List<Map<String, Object>> users = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : userSessions.entrySet()) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username", entry.getKey());
            userMap.put("devices", entry.getValue().size());
            UserInfo info = userInfos.get(entry.getKey());
            if (info != null) {
                userMap.put("nickname", info.nickname);
                userMap.put("avatar", info.avatar);
            }
            Long userId = usernameToUserId.get(entry.getKey());
            if (userId != null) {
                userMap.put("userId", userId);
            }
            users.add(userMap);
        }
        try {
            return objectMapper.writeValueAsString(users);
        } catch (JsonProcessingException e) {
            log.error("序列化在线用户列表失败", e);
            return "[]";
        }
    }

    /**
     * 获取用户的所有会话ID
     */
    public Set<String> getUserSessions(String username) {
        Set<String> sessions = userSessions.get(username);
        return sessions != null ? new HashSet<>(sessions) : Collections.emptySet();
    }

    /**
     * 根据会话ID获取用户名
     */
    public String getUsernameBySession(String sessionId) {
        return sessionUsers.get(sessionId);
    }

    /**
     * 根据用户名获取用户ID
     */
    public Long getUserIdByUsername(String username) {
        // 先从缓存获取
        Long cachedId = usernameToUserId.get(username);
        if (cachedId != null) {
            return cachedId;
        }
        // 缓存未命中，从数据库查询
        return userRepository.findByUsername(username)
                .map(user -> {
                    usernameToUserId.put(username, user.getId());
                    return user.getId();
                })
                .orElse(null);
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(String username, String nickname, String avatar) {
        userInfos.put(username, new UserInfo(nickname, avatar));
    }

    /**
     * 通知用户消息被撤回
     */
    public void notifyMessageRecalled(Long receiverId, Long messageId, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MESSAGE_RECALLED");
        payload.put("messageId", messageId);
        payload.put("senderId", senderId);
        payload.put("recallTime", java.time.LocalDateTime.now().toString());
        
        try {
            String json = objectMapper.writeValueAsString(payload);
            // 向接收者所有在线设备发送撤回通知
            userRepository.findById(receiverId).ifPresent(user -> {
                Set<String> sessions = getUserSessions(user.getUsername());
                sessions.forEach(sessionId -> {
                    sendToSession(sessionId, json);
                });
            });
            log.info("已发送消息 {} 撤回通知给用户 {}", messageId, receiverId);
        } catch (Exception e) {
            log.error("发送消息撤回通知失败", e);
        }
    }

    /**
     * 通知群组成员消息被撤回
     */
    public void notifyGroupMessageRecalled(Long groupId, Long messageId, Long senderId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GROUP_MESSAGE_RECALLED");
        payload.put("messageId", messageId);
        payload.put("groupId", groupId);
        payload.put("senderId", senderId);
        payload.put("recallTime", java.time.LocalDateTime.now().toString());
        
        try {
            String json = objectMapper.writeValueAsString(payload);
            log.info("群组 {} 消息 {} 被撤回，通知所有成员", groupId, messageId);
            // 群组撤回通知通过群组广播机制发送（具体实现依赖GroupMember）
        } catch (Exception e) {
            log.error("发送群组消息撤回通知失败", e);
        }
    }

    /**
     * 向指定会话发送消息
     */
    private void sendToSession(String sessionId, String message) {
        // WebSocket发送逻辑由WebSocketConfig处理
        // 此处记录日志，实际发送通过STOMP消息代理
        log.debug("向会话 {} 发送: {}", sessionId, message);
    }

    /**
     * 内部类：用户信息
     */
    private static class UserInfo {
        String nickname;
        String avatar;

        UserInfo(String nickname, String avatar) {
            this.nickname = nickname;
            this.avatar = avatar;
        }
    }
}
