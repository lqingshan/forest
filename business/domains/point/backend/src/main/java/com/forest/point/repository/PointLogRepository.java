package com.forest.point.repository;

import com.forest.point.entity.PointLogPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 提供积分流水的持久化访问能力。
 */
@Repository
public interface PointLogRepository extends JpaRepository<PointLogPO, Long> {
    Page<PointLogPO> findByUserIdOrderByCreatedTimeDesc(Long userId, Pageable pageable);

    Optional<PointLogPO> findByBizKey(String bizKey);
}
