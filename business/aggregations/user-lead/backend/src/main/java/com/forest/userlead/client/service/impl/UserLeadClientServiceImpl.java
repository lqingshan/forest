package com.forest.userlead.client.service.impl;

import com.forest.lead.entity.LeadPO;
import com.forest.lead.entity.LeadUnlockRecordPO;
import com.forest.lead.service.LeadService;
import com.forest.lead.service.LeadUnlockService;
import com.forest.point.entity.PointLogPO;
import com.forest.point.service.PointBalanceService;
import com.forest.starter.exception.BusinessException;
import com.forest.userlead.client.service.UserLeadClientService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 实现用户端线索聚合能力。
 *
 * <p>该聚合只编排 lead、unlock record 与 point，不拥有线索主数据和积分账本。</p>
 */
@Service
public class UserLeadClientServiceImpl implements UserLeadClientService {
    private static final int UNLOCK_COST = 5;
    private static final String MASKED_VALUE = "已遮挡，解锁后可见";

    private final LeadService leadService;
    private final LeadUnlockService leadUnlockService;
    private final PointBalanceService pointBalanceService;

    public UserLeadClientServiceImpl(
        LeadService leadService,
        LeadUnlockService leadUnlockService,
        PointBalanceService pointBalanceService
    ) {
        this.leadService = leadService;
        this.leadUnlockService = leadUnlockService;
        this.pointBalanceService = pointBalanceService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserLeadItem> searchPage(Long userId, UserLeadPageQuery query, Pageable pageable) {
        validateUserId(userId);
        UserLeadPageQuery safeQuery = query == null ? new UserLeadPageQuery(null, null) : query;
        Page<LeadPO> leads = leadService.searchPage(new LeadService.LeadPageQuery(safeQuery.keyword(), safeQuery.country()), pageable);
        // 批量查询当前页解锁状态，避免列表页逐条访问解锁记录。
        List<Long> leadIds = leads.getContent().stream()
            .map(LeadPO::getId)
            .toList();
        Set<Long> unlockedLeadIds = leadUnlockService.getUnlockedLeadIds(userId, leadIds);
        return leads.map(lead -> toItem(lead, unlockedLeadIds.contains(lead.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserLeadItem> searchUnlockedPage(Long userId, Pageable pageable) {
        validateUserId(userId);
        // 已解锁列表以解锁记录为准；线索被软删时跳过展示但不影响用户历史记录。
        Page<LeadUnlockRecordPO> unlockPage = leadUnlockService.getUnlockPage(userId, pageable);
        List<Long> leadIds = unlockPage.getContent().stream()
            .map(LeadUnlockRecordPO::getLeadId)
            .toList();
        Map<Long, LeadPO> leadMap = new LinkedHashMap<>();
        for (Long leadId : leadIds) {
            try {
                leadMap.put(leadId, leadService.getRequiredActiveLead(leadId));
            } catch (BusinessException ignored) {
                // Soft-deleted leads should not block the unlocked page.
            }
        }
        List<UserLeadItem> items = leadIds.stream()
            .map(leadMap::get)
            .filter(java.util.Objects::nonNull)
            .map(lead -> toItem(lead, true))
            .toList();
        return new PageImpl<>(items, pageable, unlockPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public UserLeadDetail getDetail(Long userId, Long leadId) {
        validateUserId(userId);
        LeadPO lead = leadService.getRequiredActiveLead(leadId);
        boolean unlocked = leadUnlockService.hasUnlocked(userId, leadId);
        return toDetail(lead, unlocked);
    }

    @Override
    @Transactional
    public UnlockResult unlock(Long userId, Long leadId) {
        validateUserId(userId);
        LeadPO lead = leadService.getRequiredActiveLead(leadId);
        if (leadUnlockService.hasUnlocked(userId, leadId)) {
            return new UnlockResult(true, "已解锁过，无需重复解锁", lead.getId(), pointBalanceService.getBalance(userId).getBalance());
        }

        // 先扣积分再写解锁记录；bizKey 保证同一用户同一线索不会重复扣减。
        PointBalanceService.PointChangeResult changeResult = pointBalanceService.spendPoints(
            userId,
            UNLOCK_COST,
            PointLogPO.SourceType.UNLOCK,
            leadId,
            "unlock:" + userId + ":" + leadId
        );
        leadUnlockService.createUnlockRecord(userId, leadId, UNLOCK_COST);
        return new UnlockResult(true, "解锁成功", lead.getId(), changeResult.balanceAfter());
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
    }

    private UserLeadItem toItem(LeadPO lead, boolean unlocked) {
        // 遮罩策略统一在服务端执行，避免前端绕过页面逻辑读取敏感联系方式。
        return new UserLeadItem(
            lead.getId(),
            lead.getName(),
            lead.getCategory(),
            lead.getCountry(),
            unlocked,
            unlocked ? nullToEmpty(lead.getPhone()) : mask(lead.getPhone()),
            unlocked ? nullToEmpty(lead.getWebsite()) : mask(lead.getWebsite())
        );
    }

    private UserLeadDetail toDetail(LeadPO lead, boolean unlocked) {
        return new UserLeadDetail(
            lead.getId(),
            lead.getName(),
            lead.getCategory(),
            lead.getCountry(),
            lead.getIntro(),
            unlocked,
            unlocked ? nullToEmpty(lead.getPhone()) : mask(lead.getPhone()),
            unlocked ? nullToEmpty(lead.getEmail()) : mask(lead.getEmail()),
            unlocked ? nullToEmpty(lead.getWebsite()) : mask(lead.getWebsite())
        );
    }

    private String mask(String value) {
        return value == null || value.isBlank() ? "" : MASKED_VALUE;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
