package com.forest.organization.common;

import com.forest.starter.time.ForestTime;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Generates stable business numbers for the organization domain.
 */
@Component
public class OrganizationNumberGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String nextOrganizationNo() {
        return next("ORG");
    }

    public String nextDepartmentNo() {
        return next("DEPT");
    }

    public String nextMemberNo() {
        return next("MEM");
    }

    public String nextCertificationNo() {
        return next("CERT");
    }

    private String next(String prefix) {
        String date = ForestTime.now().format(DATE_FORMAT);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
        return prefix + date + random;
    }
}
