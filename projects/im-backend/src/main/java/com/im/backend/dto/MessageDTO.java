package com.im.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderNickname;
    private String senderAvatar;
    private Long receiverId;
    private Integer msgType;
    private String conversationType;
    private Long conversationId;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
}
