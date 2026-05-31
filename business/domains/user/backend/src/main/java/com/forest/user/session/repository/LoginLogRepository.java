package com.forest.user.session.repository;

import com.forest.user.session.entity.LoginLogPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 提供登录日志持久化访问能力。
 */
@Repository
public interface LoginLogRepository extends JpaRepository<LoginLogPO, Long> {
}
