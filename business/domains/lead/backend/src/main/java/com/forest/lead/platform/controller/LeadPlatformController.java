package com.forest.lead.platform.controller;

import com.forest.lead.platform.service.LeadPlatformService;
import com.forest.lead.entity.LeadPO;
import com.forest.starter.common.Result;
import com.forest.starter.web.ForestApiPaths;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露平台端线索管理接口。
 */
@RestController
@RequestMapping(ForestApiPaths.PLATFORM + "/lead")
public class LeadPlatformController {
    private final LeadPlatformService leadPlatformService;

    public LeadPlatformController(LeadPlatformService leadPlatformService) {
        this.leadPlatformService = leadPlatformService;
    }

    @GetMapping("/page")
    public Result<Page<LeadVO>> getLeadPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String country
    ) {
        return Result.success(leadPlatformService
            .searchPage(new LeadPlatformService.LeadPlatformPageQuery(keyword, country), PageRequest.of(page, size))
            .map(LeadVO::from));
    }

    @GetMapping("/{id}")
    public Result<LeadDetailVO> getLead(@PathVariable Long id) {
        return Result.success(LeadDetailVO.from(leadPlatformService.getRequiredActiveLead(id)));
    }

    @PostMapping
    public Result<LeadDetailVO> createLead(@RequestBody LeadCreateRequest request) {
        LeadPO lead = new LeadPO();
        applyCreateRequest(lead, request);
        lead.setDeleted(0);
        return Result.success(LeadDetailVO.from(leadPlatformService.save(lead)));
    }

    @PutMapping("/{id}")
    public Result<LeadDetailVO> updateLead(@PathVariable Long id, @RequestBody LeadUpdateRequest request) {
        LeadPO lead = leadPlatformService.getRequiredActiveLead(id);
        lead.setSourceType(request.sourceType());
        lead.setKeywords(request.keywords());
        lead.setName(request.name());
        lead.setCategory(request.category());
        lead.setCountry(request.country());
        lead.setPhone(request.phone());
        lead.setEmail(request.email());
        lead.setWebsite(request.website());
        lead.setIntro(request.intro());
        return Result.success(LeadDetailVO.from(leadPlatformService.save(lead)));
    }

    @DeleteMapping("/{id}")
    public Result<ActionVO> deleteLead(@PathVariable Long id) {
        leadPlatformService.softDelete(id);
        return Result.success(new ActionVO(true));
    }

    private void applyCreateRequest(LeadPO lead, LeadCreateRequest request) {
        lead.setSourceType(request.sourceType());
        lead.setKeywords(request.keywords());
        lead.setName(request.name());
        lead.setCategory(request.category());
        lead.setCountry(request.country());
        lead.setPhone(request.phone());
        lead.setEmail(request.email());
        lead.setWebsite(request.website());
        lead.setIntro(request.intro());
    }

    /**
     * 表示平台端线索列表项。
     */
    public record LeadVO(
        Long id,
        String sourceType,
        String keywords,
        String name,
        String category,
        String country,
        String phone,
        String email,
        String website,
        String intro,
        java.time.LocalDateTime createdTime,
        java.time.LocalDateTime modifiedTime
    ) {
        public static LeadVO from(LeadPO lead) {
            return new LeadVO(
                lead.getId(),
                lead.getSourceType(),
                lead.getKeywords(),
                lead.getName(),
                lead.getCategory(),
                lead.getCountry(),
                lead.getPhone(),
                lead.getEmail(),
                lead.getWebsite(),
                lead.getIntro(),
                lead.getCreatedTime(),
                lead.getModifiedTime()
            );
        }
    }

    /**
     * 表示管理端线索详情。
     */
    public record LeadDetailVO(
        Long id,
        String sourceType,
        String keywords,
        String name,
        String category,
        String country,
        String phone,
        String email,
        String website,
        String intro,
        java.time.LocalDateTime createdTime,
        java.time.LocalDateTime modifiedTime
    ) {
        public static LeadDetailVO from(LeadPO lead) {
            return new LeadDetailVO(
                lead.getId(),
                lead.getSourceType(),
                lead.getKeywords(),
                lead.getName(),
                lead.getCategory(),
                lead.getCountry(),
                lead.getPhone(),
                lead.getEmail(),
                lead.getWebsite(),
                lead.getIntro(),
                lead.getCreatedTime(),
                lead.getModifiedTime()
            );
        }
    }

    /**
     * 表示管理端创建线索请求。
     */
    public record LeadCreateRequest(
        String sourceType,
        String keywords,
        String name,
        String category,
        String country,
        String phone,
        String email,
        String website,
        String intro
    ) {
    }

    /**
     * 表示管理端更新线索请求。
     */
    public record LeadUpdateRequest(
        String sourceType,
        String keywords,
        String name,
        String category,
        String country,
        String phone,
        String email,
        String website,
        String intro
    ) {
    }

    /**
     * 表示管理端简单动作接口的执行结果。
     */
    public record ActionVO(boolean success) {
    }
}
