package com.forest.user.session.repository;

import com.forest.user.session.entity.AuthSessionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提供登录会话持久化访问能力。
 */
@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSessionPO, Long> {
    List<AuthSessionPO> findByUserIdAndStatus(Long userId, AuthSessionPO.Status status);

    Optional<AuthSessionPO> findBySessionNo(String sessionNo);
}
