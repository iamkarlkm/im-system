package com.im.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送好友申请请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendRequest {
    @NotNull(message = "好友ID不能为空")
    private Long friendId;
}