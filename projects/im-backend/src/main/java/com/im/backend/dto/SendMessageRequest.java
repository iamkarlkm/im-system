package com.im.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消息发送请求DTO
 */
@Data
public class SendMessageRequest {
    
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;
    
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 消息类型: 1=文本, 2=图片, 3=语音, 4=视频, 5=文件
     */
    private Integer msgType = 1;
    
    /**
     * 会话类型: SINGLE=单聊, GROUP=群聊
     */
    private String conversationType = "SINGLE";
    
    /**
     * 会话ID（群聊时使用）
     */
    private Long conversationId;
}
