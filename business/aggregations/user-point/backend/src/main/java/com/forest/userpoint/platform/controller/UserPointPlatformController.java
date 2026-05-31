package com.forest.userpoint.platform.controller;

import com.forest.point.entity.PointLogPO;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import com.forest.user.user.entity.UserPO;
import com.forest.userpoint.platform.service.UserPointPlatformService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露平台端用户积分聚合接口。
 */
@RestController
@RequestMapping(ForestApiPaths.PLATFORM + "/user-point")
public class UserPointPlatformController {
    private final UserPointPlatformService userPointPlatformService;

    public UserPointPlatformController(UserPointPlatformService userPointPlatformService) {
        this.userPointPlatformService = userPointPlatformService;
    }

    @GetMapping("/page")
    public Result<Page<UserPointRowVO>> getUserPointPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Long id,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) UserPO.Status status
    ) {
        return Result.success(userPointPlatformService.searchPage(
            new UserPointPlatformService.UserPointPageQuery(id, name, phone, email, status),
            PageRequest.of(page, size)
        ).map(UserPointRowVO::from));
    }

    @GetMapping("/{userId}")
    public Result<UserPointDetailVO> getUserPointDetail(@PathVariable Long userId) {
        return Result.success(UserPointDetailVO.from(userPointPlatformService.getDetail(userId)));
    }

    @GetMapping("/{userId}/logs/page")
    public Result<Page<UserPointLogVO>> getUserPointLogPage(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return Result.success(userPointPlatformService.getLogPage(userId, PageRequest.of(page, size)).map(UserPointLogVO::from));
    }

    /**
     * 表示聚合列表行。
     */
    public record UserPointRowVO(UserPointUserVO user, UserPointPointsVO points) {
        public static UserPointRowVO from(UserPointPlatformService.UserPointRow row) {
            return new UserPointRowVO(UserPointUserVO.from(row.user()), UserPointPointsVO.from(row.points()));
        }
    }

    /**
     * 表示聚合详情。
     */
    public record UserPointDetailVO(UserPointUserVO user, UserPointPointsVO points) {
        public static UserPointDetailVO from(UserPointPlatformService.UserPointDetail detail) {
            return new UserPointDetailVO(UserPointUserVO.from(detail.user()), UserPointPointsVO.from(detail.points()));
        }
    }

    /**
     * 表示聚合结果中的用户信息。
     */
    public record UserPointUserVO(
        Long id,
        String name,
        String avatar,
        String phone,
        String email,
        String status,
        boolean adminUser,
        boolean user
    ) {
        public static UserPointUserVO from(UserPointPlatformService.UserSummary user) {
            return new UserPointUserVO(
                user.id(),
                user.name(),
                user.avatar(),
                user.phone(),
                user.email(),
                user.status(),
                user.adminUser(),
                user.user()
            );
        }
    }

    /**
     * 表示聚合结果中的积分摘要。
     */
    public record UserPointPointsVO(
        Integer balance,
        Integer totalIncome,
        Integer totalSpend,
        java.time.LocalDateTime updatedAt
    ) {
        public static UserPointPointsVO from(UserPointPlatformService.PointSummary points) {
            return new UserPointPointsVO(
                points.balance(),
                points.totalIncome(),
                points.totalSpend(),
                points.updatedAt()
            );
        }
    }

    /**
     * 表示积分流水信息。
     */
    public record UserPointLogVO(
        Long id,
        Long userId,
        String direction,
        Integer amount,
        Integer balanceAfter,
        String sourceType,
        Long sourceId,
        String bizKey,
        java.time.LocalDateTime createdTime
    ) {
        public static UserPointLogVO from(PointLogPO log) {
            return new UserPointLogVO(
                log.getId(),
                log.getUserId(),
                log.getDirection().name(),
                log.getAmount(),
                log.getBalanceAfter(),
                log.getSourceType().name(),
                log.getSourceId(),
                log.getBizKey(),
                log.getCreatedTime()
            );
        }
    }
}
