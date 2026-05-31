package com.forest.userpoint.platform.service.impl;

import com.forest.point.entity.PointLogPO;
import com.forest.point.service.PointBalanceService;
import com.forest.starter.exception.BusinessException;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.service.UserService;
import com.forest.userpoint.platform.service.UserPointPlatformService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 实现管理端用户积分聚合能力。
 */
@Service
public class UserPointPlatformServiceImpl implements UserPointPlatformService {
    private final UserService userService;
    private final PointBalanceService pointBalanceService;

    public UserPointPlatformServiceImpl(UserService userService, PointBalanceService pointBalanceService) {
        this.userService = userService;
        this.pointBalanceService = pointBalanceService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserPointRow> searchPage(UserPointPageQuery query, Pageable pageable) {
        UserPointPageQuery safeQuery = query == null
            ? new UserPointPageQuery(null, null, null, null, null)
            : query;
        List<Long> userIds = userService.searchIds(
            new UserService.UserPageQuery(
                safeQuery.id(),
                safeQuery.name(),
                safeQuery.phone(),
                safeQuery.email(),
                safeQuery.status()
            )
        );
        if (userIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<PointBalanceService.PointBalanceSummary> pointPage = pointBalanceService.pageBalanceSummariesByUserIds(userIds, pageable);
        List<Long> pageUserIds = pointPage.getContent().stream()
            .map(PointBalanceService.PointBalanceSummary::userId)
            .toList();
        Map<Long, UserPO> userMap = userService.getUserMap(pageUserIds);
        List<UserPointRow> rows = pointPage.getContent().stream()
            .map(summary -> UserPointRow.from(
                userMap.get(summary.userId()),
                summary
            ))
            .toList();
        return new PageImpl<>(rows, pageable, pointPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public UserPointDetail getDetail(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户不存在");
        }
        UserPO user = userService.getRequiredById(userId);
        return UserPointDetail.from(user, pointBalanceService.getBalanceSummary(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointLogPO> getLogPage(Long userId, Pageable pageable) {
        userService.getRequiredById(userId);
        return pointBalanceService.getLogPage(userId, pageable);
    }

}
