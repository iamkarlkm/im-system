package com.im.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组实体类
 */
@Data
@Entity
@Table(name = "groups", indexes = {
    @Index(name = "idx_owner_id", columnList = "owner_id")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String avatar;

    @Column(length = 500)
    private String description;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /**
     * 群成员数量
     */
    @Column(name = "member_count")
    private Integer memberCount = 0;

    /**
     * 群状态: 1=正常, 0=已解散
     */
    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    @Transient
    private List<GroupMember> members;
}
