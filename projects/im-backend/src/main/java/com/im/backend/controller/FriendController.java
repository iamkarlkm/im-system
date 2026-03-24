package com.im.backend.controller;

import com.im.backend.dto.ApiResponse;
import com.im.backend.dto.FriendDTO;
import com.im.backend.dto.SendFriendRequest;
import com.im.backend.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友关系控制器
 * 处理好友申请、管理、查询等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FriendController {
    
    private final FriendService friendService;
    
    /**
     * 发送好友申请
     * POST /api/friends/request
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<FriendDTO>> sendFriendRequest(
            Authentication authentication,
            @RequestBody SendFriendRequest request) {
        try {
            String username = authentication.getName();
            log.info("用户 {} 发送好友申请给用户ID: {}", username, request.getFriendId());
            
            Long userId = getUserIdFromAuthentication(authentication);
            FriendDTO result = friendService.sendFriendRequest(userId, request.getFriendId());
            
            return ResponseEntity.ok(ApiResponse.success("好友申请已发送", result));
        } catch (Exception e) {
            log.error("发送好友申请失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取好友列表
     * GET /api/friends
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendDTO>>> getFriends(
            Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("用户 {} 获取好友列表", username);
            
            Long userId = getUserIdFromAuthentication(authentication);
            List<FriendDTO> friends = friendService.getFriends(userId);
            
            return ResponseEntity.ok(ApiResponse.success("获取好友列表成功", friends));
        } catch (Exception e) {
            log.error("获取好友列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取收到的好友申请列表
     * GET /api/friends/requests/received
     */
    @GetMapping("/requests/received")
    public ResponseEntity<ApiResponse<List<FriendDTO>>> getReceivedRequests(
            Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("用户 {} 获取收到的好友申请列表", username);
            
            Long userId = getUserIdFromAuthentication(authentication);
            List<FriendDTO> requests = friendService.getReceivedRequests(userId);
            
            return ResponseEntity.ok(ApiResponse.success("获取好友申请列表成功", requests));
        } catch (Exception e) {
            log.error("获取好友申请列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 处理好友申请（同意/拒绝）
     * PUT /api/friends/request/{requestId}/handle
     */
    @PutMapping("/request/{requestId}/handle")
    public ResponseEntity<ApiResponse<FriendDTO>> handleFriendRequest(
            Authentication authentication,
            @PathVariable Long requestId,
            @RequestParam boolean accept) {
        try {
            String username = authentication.getName();
            log.info("用户 {} 处理好友申请 {}，接受状态: {}", username, requestId, accept);
            
            Long userId = getUserIdFromAuthentication(authentication);
            FriendDTO result = friendService.handleFriendRequest(userId, requestId, accept);
            
            String message = accept ? "已同意好友申请" : "已拒绝好友申请";
            return ResponseEntity.ok(ApiResponse.success(message, result));
        } catch (Exception e) {
            log.error("处理好友申请失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除好友
     * DELETE /api/friends/{friendId}
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            Authentication authentication,
            @PathVariable Long friendId) {
        try {
            String username = authentication.getName();
            log.info("用户 {} 删除好友 {}", username, friendId);
            
            Long userId = getUserIdFromAuthentication(authentication);
            friendService.removeFriend(userId, friendId);
            
            return ResponseEntity.ok(ApiResponse.success("好友删除成功", null));
        } catch (Exception e) {
            log.error("删除好友失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 搜索好友（根据用户名）
     * GET /api/friends/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FriendDTO>>> searchFriends(
            Authentication authentication,
            @RequestParam String keyword) {
        try {
            String username = authentication.getName();
            log.info("用户 {} 搜索好友，关键词: {}", username, keyword);
            
            Long userId = getUserIdFromAuthentication(authentication);
            List<FriendDTO> results = friendService.searchFriends(userId, keyword);
            
            return ResponseEntity.ok(ApiResponse.success("搜索完成", results));
        } catch (Exception e) {
            log.error("搜索好友失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 检查是否已是好友
     * GET /api/friends/check/{friendId}
     */
    @GetMapping("/check/{friendId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFriendStatus(
            Authentication authentication,
            @PathVariable Long friendId) {
        try {
            String username = authentication.getName();
            log.info("用户 {} 检查与用户ID {} 的好友状态", username, friendId);
            
            Long userId = getUserIdFromAuthentication(authentication);
            boolean isFriend = friendService.isFriend(userId, friendId);
            
            return ResponseEntity.ok(ApiResponse.success("检查完成", isFriend));
        } catch (Exception e) {
            log.error("检查好友状态失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取发送的好友申请列表
     * GET /api/friends/requests/sent
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<ApiResponse<List<FriendDTO>>> getSentRequests(
            Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("用户 {} 获取发送的好友申请列表", username);
            
            Long userId = getUserIdFromAuthentication(authentication);
            List<FriendDTO> requests = friendService.getSentRequests(userId);
            
            return ResponseEntity.ok(ApiResponse.success("获取发送的申请列表成功", requests));
        } catch (Exception e) {
            log.error("获取发送的申请列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 撤回好友申请
     * DELETE /api/friends/request/{requestId}
     */
    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
            Authentication authentication,
            @PathVariable Long requestId) {
        try {
            String username = authentication.getName();
            log.info("用户 {} 撤回好友申请 {}", username, requestId);
            
            Long userId = getUserIdFromAuthentication(authentication);
            friendService.cancelFriendRequest(userId, requestId);
            
            return ResponseEntity.ok(ApiResponse.success("好友申请已撤回", null));
        } catch (Exception e) {
            log.error("撤回好友申请失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("用户未认证");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.im.backend.model.User) {
            return ((com.im.backend.model.User) principal).getId();
        } else if (principal instanceof String) {
            // 如果是用户名，需要查询数据库获取用户ID
            try {
                // 这里应该调用UserService获取用户ID
                // 暂时返回固定值
                return 1L;
            } catch (Exception e) {
                throw new RuntimeException("无法获取用户ID");
            }
        } else {
            throw new RuntimeException("无法识别的认证信息");
        }
    }
}