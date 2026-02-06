package com.example.demo.resiliencetemp.config.model;

/**
 * Configuration model for RateLimiter
 */
public class RateLimiterConfigModel {

    private Integer limitForPeriod;
    private String limitRefreshPeriod;
    private String timeoutDuration;

    public Integer getLimitForPeriod() {
        return limitForPeriod;
    }

    public void setLimitForPeriod(Integer limitForPeriod) {
        this.limitForPeriod = limitForPeriod;
    }

    public String getLimitRefreshPeriod() {
        return limitRefreshPeriod;
    }

    public void setLimitRefreshPeriod(String limitRefreshPeriod) {
        this.limitRefreshPeriod = limitRefreshPeriod;
    }

    public String getTimeoutDuration() {
        return timeoutDuration;
    }

    public void setTimeoutDuration(String timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
    }
}

