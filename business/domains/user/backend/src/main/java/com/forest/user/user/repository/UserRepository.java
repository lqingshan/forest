package com.forest.user.user.repository;

import com.forest.user.user.entity.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 提供用户的持久化访问能力。
 */
@Repository
public interface UserRepository extends JpaRepository<UserPO, Long>, JpaSpecificationExecutor<UserPO> {
    boolean existsByAvatarAndDeleted(String avatar, Integer deleted);
}
