package com.forest.recharge.packagecfg;

import com.forest.starter.exception.BusinessException;

import java.util.List;

/**
 * 提供固定充值套餐目录。
 */
public final class RechargePackageCatalog {
    private static final List<RechargePackageDefinition> PACKAGES = List.of(
        new RechargePackageDefinition("starter", "新客包", 1, 99),
        new RechargePackageDefinition("growth", "成长包", 2990, 299),
        new RechargePackageDefinition("pro", "专业包", 4990, 499)
    );

    private RechargePackageCatalog() {
    }

    public static List<RechargePackageDefinition> list() {
        return PACKAGES;
    }

    public static RechargePackageDefinition getRequired(String code) {
        return PACKAGES.stream()
            .filter(pkg -> pkg.code().equals(code))
            .findFirst()
            .orElseThrow(() -> new BusinessException("充值套餐不存在"));
    }
}
