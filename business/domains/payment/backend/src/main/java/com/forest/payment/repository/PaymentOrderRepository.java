package com.forest.payment.repository;

import com.forest.payment.entity.PaymentOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 提供支付单持久化访问能力。
 */
@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrderPO, Long> {
    Optional<PaymentOrderPO> findByOutTradeNo(String outTradeNo);

    List<PaymentOrderPO> findByBizTypeAndBizOrderIdAndStatusIn(
        String bizType,
        Long bizOrderId,
        Collection<PaymentOrderPO.Status> statuses
    );
}
