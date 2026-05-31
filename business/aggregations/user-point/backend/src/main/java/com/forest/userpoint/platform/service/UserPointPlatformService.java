package com.forest.userpoint.platform.service;

import com.forest.point.entity.PointLogPO;
import com.forest.point.service.PointBalanceService;
import com.forest.user.user.entity.UserPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 定义管理端用户积分聚合能力。
 */
public interface UserPointPlatformService {
    Page<UserPointRow> searchPage(UserPointPageQuery query, Pageable pageable);

    UserPointDetail getDetail(Long userId);

    Page<PointLogPO> getLogPage(Long userId, Pageable pageable);

    /**
     * 表示用户积分聚合分页查询条件。
     */
    record UserPointPageQuery(
        Long id,
        String name,
        String phone,
        String email,
        UserPO.Status status
    ) {
    }

    /**
     * 表示聚合列表行。
     */
    record UserPointRow(UserSummary user, PointSummary points) {
        public static UserPointRow from(UserPO user, PointBalanceService.PointBalanceSummary pointSummary) {
            return new UserPointRow(UserSummary.from(user), PointSummary.from(pointSummary));
        }
    }

    /**
     * 表示聚合详情。
     */
    record UserPointDetail(UserSummary user, PointSummary points) {
        public static UserPointDetail from(UserPO user, PointBalanceService.PointBalanceSummary pointSummary) {
            return new UserPointDetail(UserSummary.from(user), PointSummary.from(pointSummary));
        }
    }

    /**
     * 表示用户基础信息摘要。
     */
    record UserSummary(
        Long id,
        String name,
        String avatar,
        String phone,
        String email,
        String status,
        boolean adminUser,
        boolean user
    ) {
        public static UserSummary from(UserPO user) {
            return new UserSummary(
                user.getId(),
                user.getName(),
                user.getAvatar(),
                user.getPhone(),
                user.getEmail(),
                user.getStatus().name(),
                true,
                true
            );
        }
    }

    /**
     * 表示积分摘要信息。
     */
    record PointSummary(
        Integer balance,
        Integer totalIncome,
        Integer totalSpend,
        java.time.LocalDateTime updatedAt
    ) {
        public static PointSummary from(PointBalanceService.PointBalanceSummary summary) {
            return new PointSummary(
                summary.balance(),
                summary.totalIncome(),
                summary.totalSpend(),
                summary.updatedAt()
            );
        }
    }
}
