package com.forest.userlead.client.controller;

import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import com.forest.starter.auth.context.CurrentPrincipal;
import com.forest.userlead.client.service.UserLeadClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露用户端线索聚合接口。
 *
 * <p>该接口面向微信小程序，负责把线索基础资料、解锁状态和联系方式遮罩组合成一个用户视图。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.CLIENT + "/user-lead")
public class UserLeadClientController {
    private final UserLeadClientService userLeadClientService;
    private final CurrentPrincipal currentAuth;

    public UserLeadClientController(UserLeadClientService userLeadClientService, CurrentPrincipal currentAuth) {
        this.userLeadClientService = userLeadClientService;
        this.currentAuth = currentAuth;
    }

    @GetMapping("/page")
    public Result<Page<UserLeadVO>> getLeadPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String country
    ) {
        // 列表页允许未解锁线索出现，但联系方式是否遮罩由聚合服务统一决定。
        return Result.success(userLeadClientService.searchPage(
            requireUserId(),
            new UserLeadClientService.UserLeadPageQuery(keyword, country),
            PageRequest.of(page, size)
        ).map(UserLeadVO::from));
    }

    @GetMapping("/unlocked/page")
    public Result<Page<UserLeadVO>> getUnlockedLeadPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        // “我的已解锁线索”只读取历史解锁记录，不产生新的积分扣减。
        return Result.success(userLeadClientService.searchUnlockedPage(
            requireUserId(),
            PageRequest.of(page, size)
        ).map(UserLeadVO::from));
    }

    @GetMapping("/{id}")
    public Result<UserLeadDetailVO> getLeadDetail(@PathVariable Long id) {
        // 详情页也通过同一聚合结果返回遮罩字段，避免前端自行判断敏感信息。
        return Result.success(UserLeadDetailVO.from(userLeadClientService.getDetail(requireUserId(), id)));
    }

    @PostMapping("/{id}/unlock")
    public Result<UnlockVO> unlockLead(@PathVariable Long id) {
        // 解锁动作必须在后端事务内完成扣积分和解锁记录创建。
        return Result.success(UnlockVO.from(userLeadClientService.unlock(requireUserId(), id)));
    }

    private Long requireUserId() {
        return currentAuth.requireUserId("用户未登录");
    }

    /**
     * 表示用户端线索列表项。
     */
    public record UserLeadVO(
        Long id,
        String name,
        String category,
        String country,
        boolean unlocked,
        String phone,
        String website
    ) {
        public static UserLeadVO from(UserLeadClientService.UserLeadItem item) {
            return new UserLeadVO(
                item.id(),
                item.name(),
                item.category(),
                item.country(),
                item.unlocked(),
                item.phone(),
                item.website()
            );
        }
    }

    /**
     * 表示用户端线索详情。
     */
    public record UserLeadDetailVO(
        Long id,
        String name,
        String category,
        String country,
        String intro,
        boolean unlocked,
        String phone,
        String email,
        String website
    ) {
        public static UserLeadDetailVO from(UserLeadClientService.UserLeadDetail detail) {
            return new UserLeadDetailVO(
                detail.id(),
                detail.name(),
                detail.category(),
                detail.country(),
                detail.intro(),
                detail.unlocked(),
                detail.phone(),
                detail.email(),
                detail.website()
            );
        }
    }

    /**
     * 表示线索解锁结果。
     */
    public record UnlockVO(boolean success, String message, Long leadId, Integer balanceAfter) {
        public static UnlockVO from(UserLeadClientService.UnlockResult result) {
            return new UnlockVO(result.success(), result.message(), result.leadId(), result.balanceAfter());
        }
    }
}
