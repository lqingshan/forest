package com.forest.point.event;

import com.forest.business.common.event.account.AccountLoginSide;
import com.forest.business.common.event.account.AccountLoginSucceededEvent;
import com.forest.point.service.PointBalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 监听用户端登录成功事件并预热积分余额。
 */
@Component
public class PointAccountLoginEventListener {
    private static final Logger log = LoggerFactory.getLogger(PointAccountLoginEventListener.class);

    private final PointBalanceService pointBalanceService;

    public PointAccountLoginEventListener(PointBalanceService pointBalanceService) {
        this.pointBalanceService = pointBalanceService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountLoginSucceeded(AccountLoginSucceededEvent event) {
        if (event.side() != AccountLoginSide.CLIENT) {
            return;
        }

        try {
            pointBalanceService.ensureBalance(event.userId());
        } catch (RuntimeException ex) {
            log.warn("Failed to initialize point balance after account login. userId={}, accountId={}",
                event.userId(), event.accountId(), ex);
        }
    }
}
