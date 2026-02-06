package com.example.demo.resiliencetemp.config.model;

/**
 * Configuration model for TimeLimiter
 */
public class TimeLimiterConfigModel {

    private String timeoutDuration;
    private Boolean cancelRunningFuture;

    public TimeLimiterConfigModel() {
    }

    public TimeLimiterConfigModel(String timeoutDuration, Boolean cancelRunningFuture) {
        this.timeoutDuration = timeoutDuration;
        this.cancelRunningFuture = cancelRunningFuture;
    }

    public String getTimeoutDuration() {
        return timeoutDuration;
    }

    public void setTimeoutDuration(String timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
    }

    public Boolean getCancelRunningFuture() {
        return cancelRunningFuture;
    }

    public void setCancelRunningFuture(Boolean cancelRunningFuture) {
        this.cancelRunningFuture = cancelRunningFuture;
    }
}

