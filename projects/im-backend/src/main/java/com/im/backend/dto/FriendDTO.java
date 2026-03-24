package com.im.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 好友DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendDTO {
    private Long id;
    private Long userId;
    private UserDTO user;
    private Long friendId;
    private UserDTO friend;
    private Integer status;  // 0=待确认, 1=已是好友
    private LocalDateTime createTime;
}
