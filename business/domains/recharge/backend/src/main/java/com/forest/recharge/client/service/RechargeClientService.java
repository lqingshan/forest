package com.forest.recharge.client.service;

import com.forest.recharge.entity.RechargeOrderPO;
import com.forest.recharge.packagecfg.RechargePackageDefinition;

import java.util.List;

/**
 * 定义用户端充值能力。
 */
public interface RechargeClientService {
    List<RechargePackageDefinition> getPackages();

    RechargeOrderPO createOrder(Long userId, String packageCode);

    RechargeOrderPO getOrder(Long userId, Long orderId);
}
