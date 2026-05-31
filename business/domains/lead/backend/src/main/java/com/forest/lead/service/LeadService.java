package com.forest.lead.service;

import com.forest.lead.entity.LeadPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 定义线索查询与维护能力。
 */
public interface LeadService {
    Page<LeadPO> searchPage(LeadPageQuery pageQuery, Pageable pageable);

    LeadPO getRequiredActiveLead(Long leadId);

    LeadPO save(LeadPO lead);

    void softDelete(Long leadId);

    /**
     * 表示线索分页查询条件。
     */
    record LeadPageQuery(String keyword, String country) {
    }
}
