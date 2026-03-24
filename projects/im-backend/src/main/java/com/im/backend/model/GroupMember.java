package com.im.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 群组成员实体类
 */
@Data
@Entity
@Table(name = "group_members", indexes = {
    @Index(name = "idx_group_id", columnList = "group_id"),
    @Index(name = "idx_member_id", columnList = "member_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_group_member", columnNames = {"group_id", "member_id"})
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * 成员昵称（在群内的显示名）
     */
    @Column(length = 100)
    private String nickname;

    /**
     * 角色: OWNER=群主, ADMIN=管理员, MEMBER=普通成员
     */
    @Column(nullable = false, length = 20)
    private String role = "MEMBER";

    /**
     * 入群时间
     */
    @Column(name = "join_time", nullable = false)
    private LocalDateTime joinTime;

    /**
     * 状态: 1=正常, 0=已退群, 2=被移除
     */
    @Column(nullable = false)
    private Integer status = 1;

    @PrePersist
    protected void onCreate() {
        joinTime = LocalDateTime.now();
    }
}
