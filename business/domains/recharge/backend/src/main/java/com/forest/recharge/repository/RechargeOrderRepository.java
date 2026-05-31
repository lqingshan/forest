package com.forest.recharge.repository;

import com.forest.recharge.entity.RechargeOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 提供充值主单持久化访问能力。
 */
@Repository
public interface RechargeOrderRepository extends JpaRepository<RechargeOrderPO, Long> {
    Optional<RechargeOrderPO> findByIdAndUserId(Long id, Long userId);

    Optional<RechargeOrderPO> findByRechargeNo(String rechargeNo);
}
