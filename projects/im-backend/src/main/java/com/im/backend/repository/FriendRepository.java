package com.im.backend.repository;

import com.im.backend.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 好友关系数据访问层
 */
@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    
    /**
     * 查找用户的所有好友
     */
    @Query("SELECT f FROM Friend f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = 1")
    List<Friend> findFriendsByUserId(@Param("userId") Long userId);
    
    /**
     * 检查是否已经是好友关系
     */
    @Query("SELECT f FROM Friend f WHERE ((f.userId = :userId1 AND f.friendId = :userId2) OR (f.userId = :userId2 AND f.friendId = :userId1)) AND f.status = 1")
    Optional<Friend> findFriendship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * 查找用户收到的申请
     */
    List<Friend> findByFriendIdAndStatus(Long friendId, Integer status);
    
    /**
     * 查找用户发出的申请
     */
    List<Friend> findByUserIdAndStatus(Long userId, Integer status);
    
    /**
     * 检查申请是否存在
     */
    @Query("SELECT f FROM Friend f WHERE f.userId = :userId AND f.friendId = :friendId AND f.status = :status")
    Optional<Friend> findApplication(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("status") Integer status);
}
