package com.im.backend.repository;

import com.im.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据昵称模糊搜索用户
     */
    List<User> findByNicknameContaining(String nickname);
    
    /**
     * 根据用户名模糊搜索（排除自己）
     */
    List<User> findByUsernameContainingAndIdNot(String username, Long id);
    
    /**
     * 查找在线用户
     */
    List<User> findByStatus(Integer status);
}
