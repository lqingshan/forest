package com.forest.lead.repository;

import com.forest.lead.entity.LeadPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 提供基于 PostgreSQL 原生能力的线索关键词搜索。
 */
public interface LeadKeywordSearchRepository {
    Page<LeadPO> searchByKeyword(String keyword, String country, Pageable pageable);

    boolean supportsNativeKeywordSearch();
}
