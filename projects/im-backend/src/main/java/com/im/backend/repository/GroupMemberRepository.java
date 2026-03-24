package com.im.backend.repository;

import com.im.backend.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 群组成员数据访问层
 */
@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    /**
     * 查找群组成员列表
     */
    List<GroupMember> findByGroupId(Long groupId);
    
    /**
     * 查找用户加入的群组
     */
    List<GroupMember> findByUserId(Long userId);
    
    /**
     * 检查用户是否在群组中
     */
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    
    /**
     * 查找群组成员
     */
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    
    /**
     * 统计群组成员数量
     */
    long countByGroupId(Long groupId);
    
    /**
     * 删除用户在群组的成员记录
     */
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
}
