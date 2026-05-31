package com.forest.user.useraccount.repository;

import com.forest.user.useraccount.entity.UserAccountPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提供用户账号绑定关系的持久化访问能力。
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountPO, Long> {
    Optional<UserAccountPO> findByAccountId(Long accountId);

    List<UserAccountPO> findByUserId(Long userId);
}
