package com.forest.userpoint.platform.service.impl;

import com.forest.point.entity.PointLogPO;
import com.forest.point.service.PointBalanceService;
import com.forest.starter.exception.BusinessException;
import com.forest.user.user.entity.UserPO;
import com.forest.user.user.service.UserService;
import com.forest.userpoint.platform.service.UserPointPlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 验证用户积分聚合服务的跨域编排规则。
 */
@ExtendWith(MockitoExtension.class)
class UserPointPlatformServiceImplTest {
    @Mock
    private UserService userService;

    @Mock
    private PointBalanceService pointBalanceService;

    @InjectMocks
    private UserPointPlatformServiceImpl userPointPlatformService;

    @Test
    void searchPageReturnsEmptyPageWhenNoUsersMatch() {
        Pageable pageable = PageRequest.of(0, 20);
        when(userService.searchIds(any())).thenReturn(List.of());

        Page<UserPointPlatformService.UserPointRow> page = userPointPlatformService.searchPage(null, pageable);

        assertTrue(page.isEmpty());
        assertEquals(0, page.getTotalElements());

        ArgumentCaptor<UserService.UserPageQuery> queryCaptor = ArgumentCaptor.forClass(UserService.UserPageQuery.class);
        verify(userService).searchIds(queryCaptor.capture());
        UserService.UserPageQuery query = queryCaptor.getValue();
        assertNull(query.id());
        assertNull(query.name());
        assertNull(query.phone());
        assertNull(query.email());
        assertNull(query.status());
        verifyNoInteractions(pointBalanceService);
    }

    @Test
    void searchPageUsesUserFilterAndPointOwnedPagination() {
        Pageable pageable = PageRequest.of(1, 2);
        UserPointPlatformService.UserPointPageQuery query = new UserPointPlatformService.UserPointPageQuery(
            9L,
            "Alice",
            "13800138000",
            "alice@forest.example",
            UserPO.Status.FROZEN
        );
        PointBalanceService.PointBalanceSummary firstSummary = new PointBalanceService.PointBalanceSummary(
            2L,
            30,
            100,
            70,
            LocalDateTime.of(2026, 4, 18, 10, 0)
        );
        PointBalanceService.PointBalanceSummary secondSummary = new PointBalanceService.PointBalanceSummary(
            1L,
            12,
            20,
            8,
            LocalDateTime.of(2026, 4, 18, 11, 0)
        );
        when(userService.searchIds(any())).thenReturn(List.of(1L, 2L, 3L));
        when(pointBalanceService.pageBalanceSummariesByUserIds(eq(List.of(1L, 2L, 3L)), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(firstSummary, secondSummary), pageable, 5));
        when(userService.getUserMap(eq(List.of(2L, 1L)))).thenReturn(Map.of(
            1L, user(1L, "Bob", UserPO.Status.ACTIVE),
            2L, user(2L, "Alice", UserPO.Status.FROZEN)
        ));

        Page<UserPointPlatformService.UserPointRow> page = userPointPlatformService.searchPage(query, pageable);

        assertEquals(5, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2L, page.getContent().get(0).user().id());
        assertEquals("Alice", page.getContent().get(0).user().name());
        assertEquals("FROZEN", page.getContent().get(0).user().status());
        assertEquals(30, page.getContent().get(0).points().balance());
        assertEquals(1L, page.getContent().get(1).user().id());
        assertEquals("Bob", page.getContent().get(1).user().name());
        assertEquals(12, page.getContent().get(1).points().balance());

        ArgumentCaptor<UserService.UserPageQuery> queryCaptor = ArgumentCaptor.forClass(UserService.UserPageQuery.class);
        verify(userService).searchIds(queryCaptor.capture());
        UserService.UserPageQuery forwardedQuery = queryCaptor.getValue();
        assertEquals(query.id(), forwardedQuery.id());
        assertEquals(query.name(), forwardedQuery.name());
        assertEquals(query.phone(), forwardedQuery.phone());
        assertEquals(query.email(), forwardedQuery.email());
        assertEquals(query.status(), forwardedQuery.status());
        verify(pointBalanceService).pageBalanceSummariesByUserIds(List.of(1L, 2L, 3L), pageable);
        verify(userService).getUserMap(List.of(2L, 1L));
    }

    @Test
    void getDetailComposesUserAndPointSummary() {
        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 18, 12, 0);
        UserPO user = user(5L, "Carol", UserPO.Status.ACTIVE);
        PointBalanceService.PointBalanceSummary points = new PointBalanceService.PointBalanceSummary(5L, 88, 120, 32, updatedAt);
        when(userService.getRequiredById(5L)).thenReturn(user);
        when(pointBalanceService.getBalanceSummary(5L)).thenReturn(points);

        UserPointPlatformService.UserPointDetail detail = userPointPlatformService.getDetail(5L);

        assertEquals(5L, detail.user().id());
        assertEquals("Carol", detail.user().name());
        assertEquals("ACTIVE", detail.user().status());
        assertEquals(88, detail.points().balance());
        assertEquals(120, detail.points().totalIncome());
        assertEquals(32, detail.points().totalSpend());
        assertEquals(updatedAt, detail.points().updatedAt());
    }

    @Test
    void getDetailRejectsNullUserIdBeforeCallingDomains() {
        BusinessException exception = assertThrows(BusinessException.class, () -> userPointPlatformService.getDetail(null));

        assertEquals("用户不存在", exception.getMessage());
        verifyNoInteractions(userService, pointBalanceService);
    }

    @Test
    void getLogPageValidatesUserBeforeReturningPointLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        PointLogPO log = new PointLogPO();
        log.setId(11L);
        log.setUserId(7L);
        when(userService.getRequiredById(7L)).thenReturn(user(7L, "Dave", UserPO.Status.ACTIVE));
        Page<PointLogPO> logPage = new PageImpl<>(List.of(log), pageable, 1);
        when(pointBalanceService.getLogPage(7L, pageable)).thenReturn(logPage);

        Page<PointLogPO> result = userPointPlatformService.getLogPage(7L, pageable);

        assertSame(logPage, result);
        verify(userService).getRequiredById(7L);
        verify(pointBalanceService).getLogPage(7L, pageable);
    }

    private UserPO user(Long id, String name, UserPO.Status status) {
        UserPO user = new UserPO();
        user.setId(id);
        user.setName(name);
        user.setAvatar("https://forest.example/avatar/" + id + ".png");
        user.setPhone("1380013800" + id);
        user.setEmail(name.toLowerCase() + "@forest.example");
        user.setStatus(status);
        return user;
    }
}
