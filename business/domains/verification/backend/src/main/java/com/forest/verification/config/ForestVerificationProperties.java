package com.forest.verification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration for Redis-backed verification features.
 */
@ConfigurationProperties(prefix = "forest.verification")
public class ForestVerificationProperties {
    private Sms sms = new Sms();

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public static class Sms {
        private String code = "121314";
        private Duration codeTtl = Duration.ofMinutes(5);
        private int maxAttempts = 5;
        private Duration sendInterval = Duration.ofSeconds(60);
        private int dailyLimit = 10;
        private Duration ticketTtl = Duration.ofMinutes(10);

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Duration getCodeTtl() {
            return codeTtl;
        }

        public void setCodeTtl(Duration codeTtl) {
            this.codeTtl = codeTtl;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getSendInterval() {
            return sendInterval;
        }

        public void setSendInterval(Duration sendInterval) {
            this.sendInterval = sendInterval;
        }

        public int getDailyLimit() {
            return dailyLimit;
        }

        public void setDailyLimit(int dailyLimit) {
            this.dailyLimit = dailyLimit;
        }

        public Duration getTicketTtl() {
            return ticketTtl;
        }

        public void setTicketTtl(Duration ticketTtl) {
            this.ticketTtl = ticketTtl;
        }
    }
}
