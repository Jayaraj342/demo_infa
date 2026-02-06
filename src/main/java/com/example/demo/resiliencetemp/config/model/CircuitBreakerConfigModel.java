package com.example.demo.resiliencetemp.config.model;

/**
 * Configuration model for CircuitBreaker
 */
public class CircuitBreakerConfigModel {

    private String slidingWindowType;
    private Integer slidingWindowSize;
    private Integer minimumNumberOfCalls;
    private Double failureRateThreshold;
    private String waitDurationInOpenState;
    private Integer permittedNumberOfCallsInHalfOpenState;
    private Boolean automaticTransitionFromOpenToHalfOpenEnabled;
    private String slowCallDurationThreshold;
    private Double slowCallRateThreshold;
    private String[] ignoreExceptions;
    private String[] recordExceptions;

    public String getSlidingWindowType() {
        return slidingWindowType;
    }

    public void setSlidingWindowType(String slidingWindowType) {
        this.slidingWindowType = slidingWindowType;
    }

    public Integer getSlidingWindowSize() {
        return slidingWindowSize;
    }

    public void setSlidingWindowSize(Integer slidingWindowSize) {
        this.slidingWindowSize = slidingWindowSize;
    }

    public Integer getMinimumNumberOfCalls() {
        return minimumNumberOfCalls;
    }

    public void setMinimumNumberOfCalls(Integer minimumNumberOfCalls) {
        this.minimumNumberOfCalls = minimumNumberOfCalls;
    }

    public Double getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public void setFailureRateThreshold(Double failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public String getWaitDurationInOpenState() {
        return waitDurationInOpenState;
    }

    public void setWaitDurationInOpenState(String waitDurationInOpenState) {
        this.waitDurationInOpenState = waitDurationInOpenState;
    }

    public Integer getPermittedNumberOfCallsInHalfOpenState() {
        return permittedNumberOfCallsInHalfOpenState;
    }

    public void setPermittedNumberOfCallsInHalfOpenState(Integer permittedNumberOfCallsInHalfOpenState) {
        this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
    }

    public Boolean getAutomaticTransitionFromOpenToHalfOpenEnabled() {
        return automaticTransitionFromOpenToHalfOpenEnabled;
    }

    public void setAutomaticTransitionFromOpenToHalfOpenEnabled(Boolean automaticTransitionFromOpenToHalfOpenEnabled) {
        this.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
    }

    public String getSlowCallDurationThreshold() {
        return slowCallDurationThreshold;
    }

    public void setSlowCallDurationThreshold(String slowCallDurationThreshold) {
        this.slowCallDurationThreshold = slowCallDurationThreshold;
    }

    public Double getSlowCallRateThreshold() {
        return slowCallRateThreshold;
    }

    public void setSlowCallRateThreshold(Double slowCallRateThreshold) {
        this.slowCallRateThreshold = slowCallRateThreshold;
    }

    public String[] getIgnoreExceptions() {
        return ignoreExceptions == null ? null : ignoreExceptions.clone();
    }

    public void setIgnoreExceptions(String[] ignoreExceptions) {
        this.ignoreExceptions = ignoreExceptions == null ? null : ignoreExceptions.clone();
    }

    public String[] getRecordExceptions() {
        return recordExceptions == null ? null : recordExceptions.clone();
    }

    public void setRecordExceptions(String[] recordExceptions) {
        this.recordExceptions = recordExceptions == null ? null : recordExceptions.clone();
    }
}
