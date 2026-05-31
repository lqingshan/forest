package com.forest.notification.sms.service;

import com.forest.notification.sms.entity.SmsSendLogPO;
import com.forest.notification.sms.repository.SmsSendLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists SMS send logs in independent transactions.
 *
 * <p>SMS logs are audit data. They must survive the business exception thrown
 * after a provider failure, so each write uses {@link Propagation#REQUIRES_NEW}.</p>
 */
@Service
public class SmsSendLogWriter {
    private final SmsSendLogRepository repository;

    public SmsSendLogWriter(SmsSendLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SmsSendLogPO createPending(SmsSendLogPO log) {
        return repository.saveAndFlush(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SmsSendLogPO saveResult(SmsSendLogPO log) {
        return repository.saveAndFlush(log);
    }
}
