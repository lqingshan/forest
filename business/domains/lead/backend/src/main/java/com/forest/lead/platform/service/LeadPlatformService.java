package com.forest.lead.platform.service;

import com.forest.lead.entity.LeadPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 定义管理端线索能力。
 */
public interface LeadPlatformService {
    Page<LeadPO> searchPage(LeadPlatformPageQuery pageQuery, Pageable pageable);

    LeadPO getRequiredActiveLead(Long leadId);

    LeadPO save(LeadPO lead);

    void softDelete(Long leadId);

    /**
     * 表示线索分页查询条件。
     */
    record LeadPlatformPageQuery(String keyword, String country) {
    }
}
