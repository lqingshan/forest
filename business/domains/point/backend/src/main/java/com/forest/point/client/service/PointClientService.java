package com.forest.point.client.service;

import com.forest.point.entity.PointBalancePO;
import com.forest.point.entity.PointLogPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 定义用户端积分能力。
 */
public interface PointClientService {
    PointBalancePO getBalance(Long userId);

    Page<PointLogPO> getLogPage(Long userId, Pageable pageable);
}
