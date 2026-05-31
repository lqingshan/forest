package com.forest.lead.repository;

import com.forest.lead.entity.LeadPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 提供线索的持久化访问能力。
 */
@Repository
public interface LeadRepository extends JpaRepository<LeadPO, Long>, JpaSpecificationExecutor<LeadPO>, LeadKeywordSearchRepository {
}
