package com.forest.notification.sms.service;

import java.util.List;
import java.util.Map;

import com.forest.notification.sms.entity.SmsSendLogPO;
import com.forest.notification.sms.entity.SmsSendStatus;
import com.forest.notification.sms.repository.SmsSendLogRepository;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.sms.SmsProvider;
import com.forest.starter.sms.SmsSendResult;
import com.forest.starter.sms.SmsSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = DefaultSmsSendServiceTransactionTest.TestApplication.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:sms_send_log_test;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "forest.sms.provider=disabled",
    "forest.sms.default-sign-name=测试签名",
    "forest.sms.default-template-code=SMS_TEST"
})
class DefaultSmsSendServiceTransactionTest {
    @Autowired
    private OuterTransactionalCaller caller;

    @Autowired
    private SmsSendLogRepository repository;

    @Test
    void failedSendLogSurvivesOuterTransactionRollback() {
        assertThrows(IllegalStateException.class, caller::sendAndRollbackOuterTransaction);

        List<SmsSendLogPO> logs = repository.findAll();
        assertEquals(1, logs.size());
        assertEquals(SmsSendStatus.FAILED, logs.getFirst().getStatus());
        assertEquals("DISABLED", logs.getFirst().getProviderResponseCode());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = SmsSendLogPO.class)
    @EnableJpaRepositories(basePackageClasses = SmsSendLogRepository.class)
    @Import({
        DefaultSmsSendService.class,
        SmsSendLogWriter.class,
        SmsNumberGenerator.class,
        SmsSendLogContentRenderer.class,
        OuterTransactionalCaller.class
    })
    static class TestApplication {
        @Bean
        SmsSender smsSender() {
            return request -> SmsSendResult.failed(SmsProvider.DISABLED, null, null, "DISABLED", "短信发送能力已关闭");
        }
    }

    @Service
    static class OuterTransactionalCaller {
        private final SmsSendService smsSendService;

        OuterTransactionalCaller(SmsSendService smsSendService) {
            this.smsSendService = smsSendService;
        }

        @Transactional
        public void sendAndRollbackOuterTransaction() {
            try {
                smsSendService.send(new SmsSendCommand(
                    "cxc-commerce",
                    "cxc-commerce-buyer-mobile-h5",
                    "MOBILE_H5",
                    "LOGIN",
                    "13800138000",
                    "SMS_TEST",
                    "测试签名",
                    Map.of("code", "123456"),
                    "您的验证码为 {code}",
                    "127.0.0.1"
                ));
            } catch (BusinessException ex) {
                throw new IllegalStateException("rollback outer transaction", ex);
            }
        }
    }
}
