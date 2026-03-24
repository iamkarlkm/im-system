package com.im.backend.repository;

import com.im.backend.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 群组数据访问层
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    /**
     * 根据群名称模糊搜索
     */
    List<Group> findByGroupNameContaining(String groupName);
    
    /**
     * 查找用户创建的群组
     */
    List<Group> findByOwnerId(Long ownerId);
}
