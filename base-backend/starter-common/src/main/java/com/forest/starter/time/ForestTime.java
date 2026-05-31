package com.forest.starter.time;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * 统一 Forest 项目数据库时间语义。
 */
public final class ForestTime {
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private ForestTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE_ID);
    }

    public static LocalDateTime fromOffsetDateTime(OffsetDateTime value) {
        return value.atZoneSameInstant(ZONE_ID).toLocalDateTime();
    }
}
