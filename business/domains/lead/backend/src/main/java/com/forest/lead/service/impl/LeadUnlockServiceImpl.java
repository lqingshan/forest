package com.forest.lead.service.impl;

import com.forest.lead.entity.LeadUnlockRecordPO;
import com.forest.lead.repository.LeadUnlockRecordRepository;
import com.forest.lead.service.LeadUnlockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实现线索解锁查询与持久化能力。
 */
@Service
public class LeadUnlockServiceImpl implements LeadUnlockService {
    private final LeadUnlockRecordRepository leadUnlockRecordRepository;

    public LeadUnlockServiceImpl(LeadUnlockRecordRepository leadUnlockRecordRepository) {
        this.leadUnlockRecordRepository = leadUnlockRecordRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUnlocked(Long userId, Long leadId) {
        return leadUnlockRecordRepository.existsByUserIdAndLeadId(userId, leadId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getUnlockedLeadIds(Long userId, Collection<Long> leadIds) {
        if (userId == null || leadIds == null || leadIds.isEmpty()) {
            return Set.of();
        }
        return leadUnlockRecordRepository.findByUserIdAndLeadIdIn(userId, leadIds).stream()
            .map(LeadUnlockRecordPO::getLeadId)
            .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadUnlockRecordPO> getUnlockPage(Long userId, Pageable pageable) {
        if (userId == null) {
            return Page.empty(pageable);
        }
        return leadUnlockRecordRepository.findByUserIdOrderByUnlockTimeDesc(userId, pageable);
    }

    @Override
    @Transactional
    public LeadUnlockRecordPO createUnlockRecord(Long userId, Long leadId, Integer pointCost) {
        LeadUnlockRecordPO record = new LeadUnlockRecordPO();
        record.setUserId(userId);
        record.setLeadId(leadId);
        record.setPointCost(pointCost);
        return leadUnlockRecordRepository.save(record);
    }
}
