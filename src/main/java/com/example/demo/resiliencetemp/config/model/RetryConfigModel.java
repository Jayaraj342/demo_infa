package com.example.demo.resiliencetemp.config.model;

import io.github.resilience4j.core.IntervalBiFunction;
import io.github.resilience4j.core.IntervalFunction;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * Configuration model for Retry (mirrors resilience4j RetryConfig builder inputs).
 */
public class RetryConfigModel {

    // Core numeric/time settings
    private Integer maxAttempts;                         // default: 3
    private Duration waitDuration;                       // default: 500ms

    // Interval functions
    private IntervalFunction intervalFunction;           // default: numOfAttempts -> waitDuration
    private IntervalBiFunction intervalBiFunction;

    // Predicates
    private Predicate<Object> retryOnResultPredicate;    // default: result -> false
    private Predicate<Throwable> retryExceptionPredicate; // default: throwable -> true

    // Exception lists
    private Class<? extends Throwable>[] retryExceptions; // default: empty
    private Class<? extends Throwable>[] ignoreExceptions; // default: empty

    // Flags
    private Boolean failAfterMaxAttempts;                // default: false

    // Getters / setters
    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

    public Duration getWaitDuration() { return waitDuration; }
    public void setWaitDuration(Duration waitDuration) { this.waitDuration = waitDuration; }

    public IntervalFunction getIntervalFunction() { return intervalFunction; }
    public void setIntervalFunction(IntervalFunction intervalFunction) { this.intervalFunction = intervalFunction; }

    public IntervalBiFunction getIntervalBiFunction() { return intervalBiFunction; }
    public void setIntervalBiFunction(IntervalBiFunction intervalBiFunction) { this.intervalBiFunction = intervalBiFunction; }

    public Predicate<Object> getRetryOnResultPredicate() { return retryOnResultPredicate; }
    public void setRetryOnResultPredicate(Predicate<Object> retryOnResultPredicate) { this.retryOnResultPredicate = retryOnResultPredicate; }

    public Predicate<Throwable> getRetryExceptionPredicate() { return retryExceptionPredicate; }
    public void setRetryExceptionPredicate(Predicate<Throwable> retryExceptionPredicate) { this.retryExceptionPredicate = retryExceptionPredicate; }

    public Class<? extends Throwable>[] getRetryExceptions() {
        return retryExceptions == null ? null : retryExceptions.clone();
    }

    public void setRetryExceptions(Class<? extends Throwable>[] retryExceptions) {
        this.retryExceptions = retryExceptions == null ? null : retryExceptions.clone();
    }

    public Class<? extends Throwable>[] getIgnoreExceptions() {
        return ignoreExceptions == null ? null : ignoreExceptions.clone();
    }

    public void setIgnoreExceptions(Class<? extends Throwable>[] ignoreExceptions) {
        this.ignoreExceptions = ignoreExceptions == null ? null : ignoreExceptions.clone();
    }

    public Boolean getFailAfterMaxAttempts() { return failAfterMaxAttempts; }
    public void setFailAfterMaxAttempts(Boolean failAfterMaxAttempts) { this.failAfterMaxAttempts = failAfterMaxAttempts; }
}
