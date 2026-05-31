package com.forest.lead.repository;

import com.forest.lead.entity.LeadUnlockRecordPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 提供线索解锁记录的持久化访问能力。
 */
@Repository
public interface LeadUnlockRecordRepository extends JpaRepository<LeadUnlockRecordPO, Long> {
    boolean existsByUserIdAndLeadId(Long userId, Long leadId);

    Optional<LeadUnlockRecordPO> findByUserIdAndLeadId(Long userId, Long leadId);

    List<LeadUnlockRecordPO> findByUserIdAndLeadIdIn(Long userId, Collection<Long> leadIds);

    Page<LeadUnlockRecordPO> findByUserIdOrderByUnlockTimeDesc(Long userId, Pageable pageable);
}
