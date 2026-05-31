package com.forest.lead.service.impl;

import com.forest.lead.platform.service.LeadPlatformService;
import com.forest.lead.entity.LeadPO;
import com.forest.lead.repository.LeadRepository;
import com.forest.lead.service.LeadService;
import com.forest.lead.specification.LeadSpecifications;
import com.forest.starter.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 实现线索查询与维护能力。
 */
@Service
public class LeadServiceImpl implements LeadService, LeadPlatformService {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern PUNCTUATION_ONLY_PATTERN = Pattern.compile("^[\\p{Punct}\\s]+$");
    private static final Sort DEFAULT_PAGE_SORT = Sort.by(
        Sort.Order.desc("modifiedTime"),
        Sort.Order.desc("createdTime"),
        Sort.Order.desc("id")
    );

    private final LeadRepository leadRepository;

    public LeadServiceImpl(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadPO> searchPage(LeadPageQuery pageQuery, Pageable pageable) {
        LeadPageQuery safeQuery = pageQuery == null ? new LeadPageQuery(null, null) : pageQuery;
        return searchPage(safeQuery.keyword(), safeQuery.country(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadPO> searchPage(LeadPlatformService.LeadPlatformPageQuery pageQuery, Pageable pageable) {
        LeadPlatformService.LeadPlatformPageQuery safeQuery = pageQuery == null
            ? new LeadPlatformService.LeadPlatformPageQuery(null, null)
            : pageQuery;
        return searchPage(safeQuery.keyword(), safeQuery.country(), pageable);
    }

    private Page<LeadPO> searchPage(String keyword, String country, Pageable pageable) {
        Pageable sortedPageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_PAGE_SORT);
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedCountry = normalizeCountry(country);
        if (normalizedKeyword == null) {
            return searchSimplePage(normalizedCountry, sortedPageable);
        }
        if (PUNCTUATION_ONLY_PATTERN.matcher(normalizedKeyword).matches()) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), sortedPageable, 0);
        }
        if (leadRepository.supportsNativeKeywordSearch()) {
            return leadRepository.searchByKeyword(normalizedKeyword, normalizedCountry, sortedPageable);
        }

        Specification<LeadPO> specification = Specification
            .where(LeadSpecifications.active())
            .and(LeadSpecifications.keywordContains(normalizedKeyword))
            .and(LeadSpecifications.countryEquals(normalizedCountry));

        return leadRepository.findAll(specification, sortedPageable);
    }

    private Page<LeadPO> searchSimplePage(String country, Pageable pageable) {
        Specification<LeadPO> specification = Specification
            .where(LeadSpecifications.active())
            .and(LeadSpecifications.countryEquals(country));
        return leadRepository.findAll(specification, pageable);
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String normalized = WHITESPACE_PATTERN.matcher(keyword.trim()).replaceAll(" ");
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeCountry(String country) {
        return StringUtils.hasText(country) ? country.trim() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public LeadPO getRequiredActiveLead(Long leadId) {
        LeadPO lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new BusinessException("线索不存在"));
        if (lead.getDeleted() != null && lead.getDeleted() != 0) {
            throw new BusinessException("线索不存在");
        }
        return lead;
    }

    @Override
    @Transactional
    public LeadPO save(LeadPO lead) {
        return leadRepository.save(lead);
    }

    @Override
    @Transactional
    public void softDelete(Long leadId) {
        LeadPO lead = getRequiredActiveLead(leadId);
        lead.setDeleted(1);
        leadRepository.save(lead);
    }
}
