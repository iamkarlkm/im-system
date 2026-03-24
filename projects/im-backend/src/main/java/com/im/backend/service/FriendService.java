package com.im.backend.service;

import com.im.backend.dto.FriendDTO;
import com.im.backend.dto.UserDTO;
import com.im.backend.model.Friend;
import com.im.backend.model.User;
import com.im.backend.repository.FriendRepository;
import com.im.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 好友服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {
    
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    /**
     * 发送好友申请
     */
    @Transactional
    public FriendDTO sendFriendRequest(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new RuntimeException("不能添加自己为好友");
        }
        
        // 检查是否已经是好友或申请已存在
        friendRepository.findApplication(userId, friendId, 0)
                .ifPresent(f -> { throw new RuntimeException("申请已存在"); });
        friendRepository.findFriendship(userId, friendId)
                .ifPresent(f -> { throw new RuntimeException("你们已经是好友"); });
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Friend friendRequest = Friend.builder()
                .userId(userId)
                .friendId(friendId)
                .status(0)  // 待确认
                .build();
        
        friendRequest = friendRepository.save(friendRequest);
        log.info("用户 {} 向 {} 发送好友申请", user.getUsername(), friend.getUsername());
        
        return toFriendDTO(friendRequest, user, friend);
    }
    
    /**
     * 处理好友申请（同意/拒绝）
     */
    @Transactional
    public FriendDTO handleFriendRequest(Long userId, Long requestId, boolean accept) {
        Friend request = friendRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (!request.getFriendId().equals(userId)) {
            throw new RuntimeException("无权操作此申请");
        }
        
        if (accept) {
            request.setStatus(1);  // 已同意
            log.info("用户 {} 同意了好友申请 {}", userId, requestId);
        } else {
            request.setStatus(2);  // 已拒绝
            log.info("用户 {} 拒绝了好友申请 {}", userId, requestId);
        }
        
        request = friendRepository.save(request);
        User user = userRepository.findById(request.getUserId()).orElse(null);
        User friend = userRepository.findById(request.getFriendId()).orElse(null);
        return toFriendDTO(request, user, friend);
    }
    
    /**
     * 获取好友列表
     */
    public List<FriendDTO> getFriends(Long userId) {
        List<Friend> friends = friendRepository.findFriendsByUserId(userId);
        return friends.stream().map(f -> {
            Long friendId = f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId();
            User friend = userRepository.findById(friendId).orElse(null);
            return toFriendDTO(f, userId.equals(f.getUserId()) ? null : userRepository.findById(f.getUserId()).orElse(null), friend);
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取收到的申请列表
     */
    public List<FriendDTO> getReceivedRequests(Long userId) {
        List<Friend> requests = friendRepository.findByFriendIdAndStatus(userId, 0);
        return requests.stream().map(f -> {
            User user = userRepository.findById(f.getUserId()).orElse(null);
            return toFriendDTO(f, user, null);
        }).collect(Collectors.toList());
    }
    
    /**
     * 删除好友
     */
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        friendRepository.findFriendship(userId, friendId)
                .ifPresent(f -> {
                    friendRepository.delete(f);
                    log.info("用户 {} 删除了好友 {}", userId, friendId);
                });
    }
    
    /**
     * 搜索好友（根据用户名）
     */
    public List<FriendDTO> searchFriends(Long userId, String keyword) {
        // 获取用户的好友列表
        List<FriendDTO> friends = getFriends(userId);
        
        // 根据关键词筛选好友
        return friends.stream()
                .filter(f -> {
                    // 筛选逻辑：匹配好友的用户名
                    if (f.getFriend() != null && f.getFriend().getUsername() != null) {
                        return f.getFriend().getUsername().toLowerCase().contains(keyword.toLowerCase());
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否已是好友
     */
    public boolean isFriend(Long userId, Long friendId) {
        return friendRepository.findFriendship(userId, friendId).isPresent();
    }
    
    /**
     * 获取用户发出的好友申请列表
     */
    public List<FriendDTO> getSentRequests(Long userId) {
        List<Friend> requests = friendRepository.findByUserIdAndStatus(userId, 0);
        return requests.stream().map(f -> {
            User friend = userRepository.findById(f.getFriendId()).orElse(null);
            return toFriendDTO(f, null, friend);
        }).collect(Collectors.toList());
    }
    
    /**
     * 撤回好友申请
     */
    @Transactional
    public void cancelFriendRequest(Long userId, Long requestId) {
        Friend request = friendRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (!request.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此申请");
        }
        
        if (request.getStatus() != 0) {
            throw new RuntimeException("申请状态不允许撤回");
        }
        
        friendRepository.delete(request);
        log.info("用户 {} 撤回了好友申请 {}", userId, requestId);
    }
    
    private FriendDTO toFriendDTO(Friend friend, User user, User friendUser) {
        return FriendDTO.builder()
                .id(friend.getId())
                .userId(friend.getUserId())
                .user(user != null ? userService.toUserDTO(user) : null)
                .friendId(friend.getFriendId())
                .friend(friendUser != null ? userService.toUserDTO(friendUser) : null)
                .status(friend.getStatus())
                .createTime(friend.getCreateTime())
                .build();
    }
}
