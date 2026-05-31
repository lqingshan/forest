package com.forest.lead.specification;

import com.forest.lead.entity.LeadPO;
import com.forest.lead.entity.LeadPO_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 提供线索分页筛选场景下的小型语义化查询条件。
 */
public final class LeadSpecifications {
    private LeadSpecifications() {
    }

    public static Specification<LeadPO> active() {
        return (root, query, builder) -> builder.equal(root.get(LeadPO_.deleted), 0);
    }

    public static Specification<LeadPO> keywordContains(String keyword) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(keyword)) {
                return builder.conjunction();
            }
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            return builder.or(
                builder.like(builder.lower(root.get(LeadPO_.name)), like),
                builder.like(builder.lower(root.get(LeadPO_.category)), like),
                builder.like(builder.lower(root.get(LeadPO_.keywords)), like)
            );
        };
    }

    public static Specification<LeadPO> countryEquals(String country) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(country)) {
                return builder.conjunction();
            }
            return builder.equal(root.get(LeadPO_.country), country.trim());
        };
    }
}
