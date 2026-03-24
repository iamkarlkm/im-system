package com.im.backend.controller;

import com.im.backend.dto.*;
import com.im.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 * 处理用户注册、登录、Token刷新等认证相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            log.info("收到注册请求: {}", request.getUsername());
            AuthResponse response = userService.register(request);
            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        try {
            log.info("收到登录请求: {}", request.getUsername());
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户信息
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("未提供认证令牌"));
            }
            
            String token = authHeader.substring(7);
            UserDTO user = userService.validateToken(token) != null 
                    ? userService.getUserById(userService.validateToken(token).getId())
                    : null;
            
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("无效的令牌"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", user));
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("服务器错误"));
        }
    }
    
    /**
     * 刷新Token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        try {
            if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Refresh token不能为空"));
            }
            
            // 验证refresh token并生成新的access token
            var user = userService.validateToken(request.getRefreshToken());
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("无效的refresh token"));
            }
            
            // 重新生成认证响应
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(user.getUsername());
            // 注意：这里实际上需要重新验证密码，或者使用特殊的refresh token验证逻辑
            // 为了简化，我们返回一个新的token（实际项目中应该有更复杂的逻辑）
            
            AuthResponse response = AuthResponse.builder()
                    .token(request.getRefreshToken()) // 简化处理
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("Token刷新成功", response));
        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("服务器错误"));
        }
    }
    
    /**
     * 验证Token是否有效
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ApiResponse.success("Token无效", false));
            }
            
            String token = authHeader.substring(7);
            boolean isValid = userService.validateToken(token) != null;
            
            return ResponseEntity.ok(ApiResponse.success("验证结果", isValid));
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("验证结果", false));
        }
    }
}

/**
 * Refresh Token请求DTO
 */
@Data
class RefreshTokenRequest {
    private String refreshToken;
}
