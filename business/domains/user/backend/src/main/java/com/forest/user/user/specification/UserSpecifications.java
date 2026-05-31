package com.forest.user.user.specification;

import com.forest.user.user.entity.UserPO;
import com.forest.user.user.entity.UserPO_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * 提供用户检索场景下的小型语义化查询条件。
 */
public final class UserSpecifications {
    private UserSpecifications() {
    }

    public static Specification<UserPO> withId(Long id) {
        return (root, query, builder) -> id == null
            ? builder.conjunction()
            : builder.equal(root.get(UserPO_.id), id);
    }

    public static Specification<UserPO> nameContainsIgnoreCase(String name) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(name)) {
                return builder.conjunction();
            }
            String keyword = "%" + name.trim().toLowerCase() + "%";
            return builder.like(builder.lower(root.get(UserPO_.name)), keyword);
        };
    }

    public static Specification<UserPO> phoneContainsIgnoreCase(String phone) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(phone)) {
                return builder.conjunction();
            }
            String keyword = "%" + phone.trim().toLowerCase() + "%";
            return builder.like(builder.lower(root.get(UserPO_.phone)), keyword);
        };
    }

    public static Specification<UserPO> emailContainsIgnoreCase(String email) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(email)) {
                return builder.conjunction();
            }
            String keyword = "%" + email.trim().toLowerCase() + "%";
            return builder.like(builder.lower(root.get(UserPO_.email)), keyword);
        };
    }

    public static Specification<UserPO> withStatus(UserPO.Status status) {
        return (root, query, builder) -> status == null
            ? builder.conjunction()
            : builder.equal(root.get(UserPO_.status), status);
    }
}
