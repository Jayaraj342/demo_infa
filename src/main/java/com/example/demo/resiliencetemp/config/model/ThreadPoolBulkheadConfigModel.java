package com.example.demo.resiliencetemp.config.model;

/**
 * Configuration model for ThreadPoolBulkhead
 */
public class ThreadPoolBulkheadConfigModel {

    private Integer coreThreadPoolSize;
    private Integer maxThreadPoolSize;
    private Integer queueCapacity;
    private String keepAliveDuration;

    public Integer getCoreThreadPoolSize() {
        return coreThreadPoolSize;
    }

    public void setCoreThreadPoolSize(Integer coreThreadPoolSize) {
        this.coreThreadPoolSize = coreThreadPoolSize;
    }

    public Integer getMaxThreadPoolSize() {
        return maxThreadPoolSize;
    }

    public void setMaxThreadPoolSize(Integer maxThreadPoolSize) {
        this.maxThreadPoolSize = maxThreadPoolSize;
    }

    public Integer getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(Integer queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public void setKeepAliveDuration(String keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }
}

