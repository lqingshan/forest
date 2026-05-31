package com.forest.verification.service;

import java.time.Duration;
import java.util.Optional;

import com.forest.starter.exception.BusinessException;
import com.forest.starter.redis.client.ForestRedisJsonClient;
import com.forest.starter.redis.key.RedisKey;
import com.forest.starter.redis.key.RedisKeyFactory;
import com.forest.starter.redis.key.RedisKeyValidator;
import com.forest.verification.config.ForestVerificationProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationTicketServiceImplTest {
    private final RedisKeyFactory redisKeys = new RedisKeyFactory("forest", "cxc-commerce", true, new RedisKeyValidator());
    private final ForestRedisJsonClient redisJsonClient = mock(ForestRedisJsonClient.class);
    private final ForestVerificationProperties properties = new ForestVerificationProperties();
    private final VerificationTicketServiceImpl ticketService = new VerificationTicketServiceImpl(redisKeys, redisJsonClient, properties);

    @Test
    void issueStoresTicketWithConfiguredTtl() {
        properties.getSms().setTicketTtl(Duration.ofMinutes(3));

        VerificationTicket ticket = ticketService.issue(new IssueVerificationTicketCommand(
            VerificationScene.RESET_PASSWORD,
            "cxc-commerce",
            "cxc-commerce-buyer-mobile-h5",
            "MOBILE_H5",
            10001L,
            "13800138000",
            "PHONE"
        ));

        assertTrue(ticket.ticketNo().startsWith("VT"));
        assertEquals("RESET_PASSWORD", ticket.scene());
        verify(redisJsonClient).set(
            eq(new RedisKey("forest:cxc-commerce:verification:ticket:reset_password:" + ticket.ticketNo())),
            eq(ticket),
            eq(Duration.ofMinutes(3))
        );
    }

    @Test
    void consumeReadsAndDeletesTicketThenValidatesContext() {
        VerificationTicket ticket = new VerificationTicket(
            "VT202605140001",
            "RESET_PASSWORD",
            "cxc-commerce",
            "cxc-commerce-buyer-mobile-h5",
            "MOBILE_H5",
            10001L,
            "13800138000",
            "PHONE",
            null
        );
        when(redisJsonClient.getAndDelete(any(RedisKey.class), eq(VerificationTicket.class))).thenReturn(Optional.of(ticket));

        VerificationTicket consumed = ticketService.consume(new ConsumeVerificationTicketCommand(
            "VT202605140001",
            VerificationScene.RESET_PASSWORD,
            "cxc-commerce",
            "cxc-commerce-buyer-mobile-h5",
            "MOBILE_H5",
            10001L,
            "13800138000",
            "PHONE"
        ));

        assertEquals(ticket, consumed);
        verify(redisJsonClient).getAndDelete(
            eq(new RedisKey("forest:cxc-commerce:verification:ticket:reset_password:VT202605140001")),
            eq(VerificationTicket.class)
        );
    }

    @Test
    void consumeRejectsMismatchedContext() {
        VerificationTicket ticket = new VerificationTicket(
            "VT202605140001",
            "RESET_PASSWORD",
            "cxc-commerce",
            "cxc-commerce-buyer-mobile-h5",
            "MOBILE_H5",
            10001L,
            "13800138000",
            "PHONE",
            null
        );
        when(redisJsonClient.getAndDelete(any(RedisKey.class), eq(VerificationTicket.class))).thenReturn(Optional.of(ticket));

        assertThrows(BusinessException.class, () -> ticketService.consume(new ConsumeVerificationTicketCommand(
            "VT202605140001",
            VerificationScene.RESET_PASSWORD,
            "trade-leads",
            "cxc-commerce-buyer-mobile-h5",
            "MOBILE_H5",
            10001L,
            "13800138000",
            "PHONE"
        )));
    }
}
