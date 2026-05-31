package com.forest.starter.time;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForestTimeTest {
    @Test
    void nowUsesAsiaShanghaiTime() {
        assertEquals(ZoneId.of("Asia/Shanghai"), ForestTime.ZONE_ID);

        long secondsApart = Math.abs(
            java.time.Duration.between(
                java.time.LocalDateTime.now(ForestTime.ZONE_ID),
                ForestTime.now()
            ).toSeconds()
        );

        assertTrue(secondsApart < 2);
    }

    @Test
    void convertsOffsetTimeToAsiaShanghaiLocalTime() {
        assertEquals(
            java.time.LocalDateTime.of(2026, 4, 29, 20, 0),
            ForestTime.fromOffsetDateTime(OffsetDateTime.parse("2026-04-29T12:00:00Z"))
        );
        assertEquals(
            java.time.LocalDateTime.of(2026, 4, 29, 20, 0),
            ForestTime.fromOffsetDateTime(OffsetDateTime.parse("2026-04-29T20:00:00+08:00"))
        );
    }
}
