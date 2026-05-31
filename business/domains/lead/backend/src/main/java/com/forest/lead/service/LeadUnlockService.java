package com.forest.lead.service;

import com.forest.lead.entity.LeadUnlockRecordPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Set;

/**
 * 定义线索解锁查询与持久化能力。
 */
public interface LeadUnlockService {
    boolean hasUnlocked(Long userId, Long leadId);

    Set<Long> getUnlockedLeadIds(Long userId, Collection<Long> leadIds);

    Page<LeadUnlockRecordPO> getUnlockPage(Long userId, Pageable pageable);

    LeadUnlockRecordPO createUnlockRecord(Long userId, Long leadId, Integer pointCost);
}
