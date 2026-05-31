package com.forest.userlead.client.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 定义用户端线索聚合能力。
 */
public interface UserLeadClientService {
    Page<UserLeadItem> searchPage(Long userId, UserLeadPageQuery query, Pageable pageable);

    Page<UserLeadItem> searchUnlockedPage(Long userId, Pageable pageable);

    UserLeadDetail getDetail(Long userId, Long leadId);

    UnlockResult unlock(Long userId, Long leadId);

    /**
     * 表示用户线索分页查询条件。
     */
    record UserLeadPageQuery(String keyword, String country) {
    }

    /**
     * 表示用户端线索列表项。
     */
    record UserLeadItem(
        Long id,
        String name,
        String category,
        String country,
        boolean unlocked,
        String phone,
        String website
    ) {
    }

    /**
     * 表示用户端线索详情。
     */
    record UserLeadDetail(
        Long id,
        String name,
        String category,
        String country,
        String intro,
        boolean unlocked,
        String phone,
        String email,
        String website
    ) {
    }

    /**
     * 表示线索解锁结果。
     */
    record UnlockResult(boolean success, String message, Long leadId, Integer balanceAfter) {
    }
}
