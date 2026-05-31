package com.forest.notification.sms.repository;

import com.forest.notification.sms.entity.SmsSendLogPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persists SMS send logs.
 */
@Repository
public interface SmsSendLogRepository extends JpaRepository<SmsSendLogPO, Long> {
}
