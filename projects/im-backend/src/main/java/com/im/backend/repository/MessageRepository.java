package com.im.backend.repository;

import com.im.backend.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 消息数据访问层
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * 查找与某人的聊天记录（分页）
     */
    @Query("SELECT m FROM Message m WHERE ((m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1)) AND m.conversationType = 'SINGLE' AND m.isDeleted = 0 ORDER BY m.createTime DESC")
    Page<Message> findChatHistory(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);
    
    /**
     * 查找群聊消息记录（分页）
     */
    @Query("SELECT m FROM Message m WHERE m.conversationType = 'GROUP' AND m.conversationId = :groupId AND m.isDeleted = 0 ORDER BY m.createTime DESC")
    Page<Message> findGroupMessages(@Param("groupId") Long groupId, Pageable pageable);
    
    /**
     * 查找最近消息
     */
    @Query("SELECT m FROM Message m WHERE m.id IN (SELECT MAX(m2.id) FROM Message m2 WHERE ((m2.senderId = :userId AND m2.receiverId IN :friendIds) OR (m2.senderId IN :friendIds AND m2.receiverId = :userId)) AND m2.conversationType = 'SINGLE' GROUP BY CASE WHEN m2.senderId < m2.receiverId THEN CONCAT(m2.senderId, '-', m2.receiverId) ELSE CONCAT(m2.receiverId, '-', m2.senderId) END)")
    List<Message> findLatestMessagesByFriends(@Param("userId") Long userId, @Param("friendIds") List<Long> friendIds);
    
    /**
     * 统计未读消息数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.status = 1 AND m.isDeleted = 0")
    Long countUnreadMessages(@Param("userId") Long userId);
    
    /**
     * 将消息标记为已读
     */
    @Query("UPDATE Message m SET m.status = 2 WHERE m.receiverId = :userId AND m.senderId = :senderId AND m.status = 1")
    void markAsRead(@Param("userId") Long userId, @Param("senderId") Long senderId);
}
