package com.im.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_sender", columnList = "sender_id"),
    @Index(name = "idx_receiver", columnList = "receiver_id"),
    @Index(name = "idx_conversation", columnList = "conversation_type, conversation_id"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    /**
     * 消息类型: 1=文本, 2=图片, 3=语音, 4=视频, 5=文件
     */
    @Column(nullable = false)
    private Integer msgType = 1;

    /**
     * 会话类型: SINGLE=单聊, GROUP=群聊
     */
    @Column(name = "conversation_type", nullable = false, length = 20)
    private String conversationType = "SINGLE";

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    /**
     * 消息内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 消息状态: 0=发送中, 1=已发送, 2=已读, 3=撤回
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 是否删除: 0=未删除, 1=已删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
