package com.forest.point.repository;

import com.forest.point.entity.PointBalancePO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 提供积分余额的持久化访问能力。
 */
@Repository
public interface PointBalanceRepository extends JpaRepository<PointBalancePO, Long> {
    Optional<PointBalancePO> findByUserId(Long userId);

    List<PointBalancePO> findByUserIdIn(Collection<Long> userIds);

    Page<PointBalancePO> findByUserIdIn(Collection<Long> userIds, Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update PointBalancePO pb
           set pb.balance = pb.balance + :amount,
               pb.totalIncome = pb.totalIncome + :amount,
               pb.version = pb.version + 1,
               pb.modifiedTime = :modifiedTime
         where pb.userId = :userId
           and pb.version = :version
        """)
    int addBalance(
        @Param("userId") Long userId,
        @Param("amount") Integer amount,
        @Param("version") Integer version,
        @Param("modifiedTime") LocalDateTime modifiedTime
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update PointBalancePO pb
           set pb.balance = pb.balance - :amount,
               pb.totalSpend = pb.totalSpend + :amount,
               pb.version = pb.version + 1,
               pb.modifiedTime = :modifiedTime
         where pb.userId = :userId
           and pb.version = :version
           and pb.balance >= :amount
        """)
    int spendBalance(
        @Param("userId") Long userId,
        @Param("amount") Integer amount,
        @Param("version") Integer version,
        @Param("modifiedTime") LocalDateTime modifiedTime
    );
}
