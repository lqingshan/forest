package com.forest.recharge.packagecfg;

/**
 * 表示一个可售充值套餐。
 */
public record RechargePackageDefinition(
    String code,
    String title,
    Integer amountCents,
    Integer creditedPoints
) {
}
