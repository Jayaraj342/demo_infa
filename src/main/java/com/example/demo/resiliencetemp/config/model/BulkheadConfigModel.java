package com.example.demo.resiliencetemp.config.model;

/**
 * Configuration model for Bulkhead (Semaphore-based)
 */
public class BulkheadConfigModel {

    private Integer maxConcurrentCalls;
    private String maxWaitDuration;

    public Integer getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    public String getMaxWaitDuration() {
        return maxWaitDuration;
    }

    public void setMaxWaitDuration(String maxWaitDuration) {
        this.maxWaitDuration = maxWaitDuration;
    }
}

