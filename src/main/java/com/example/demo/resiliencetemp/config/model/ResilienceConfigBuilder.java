package com.example.demo.resiliencetemp.config.model;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import java.time.Duration;

/**
 * Builder utility to convert config models to Resilience4j config objects
 */
public class ResilienceConfigBuilder {

    /**
     * Build CircuitBreakerConfig from model
     */
    public static CircuitBreakerConfig buildCircuitBreakerConfig(CircuitBreakerConfigModel model) {
        if (model == null) {
            return CircuitBreakerConfig.ofDefaults();
        }

        CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom();

        if (model.getSlidingWindowType() != null) {
            if ("TIME_BASED".equalsIgnoreCase(model.getSlidingWindowType())) {
                builder.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED);
            } else if ("COUNT_BASED".equalsIgnoreCase(model.getSlidingWindowType())) {
                builder.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED);
            }
        }

        if (model.getSlidingWindowSize() != null) {
            builder.slidingWindowSize(model.getSlidingWindowSize());
        }

        if (model.getMinimumNumberOfCalls() != null) {
            builder.minimumNumberOfCalls(model.getMinimumNumberOfCalls());
        }

        if (model.getFailureRateThreshold() != null) {
            builder.failureRateThreshold(model.getFailureRateThreshold().floatValue());
        }

        if (model.getWaitDurationInOpenState() != null) {
            builder.waitDurationInOpenState(parseDuration(model.getWaitDurationInOpenState()));
        }

        if (model.getPermittedNumberOfCallsInHalfOpenState() != null) {
            builder.permittedNumberOfCallsInHalfOpenState(model.getPermittedNumberOfCallsInHalfOpenState());
        }

        if (model.getAutomaticTransitionFromOpenToHalfOpenEnabled() != null) {
            builder.automaticTransitionFromOpenToHalfOpenEnabled(model.getAutomaticTransitionFromOpenToHalfOpenEnabled());
        }

        if (model.getSlowCallDurationThreshold() != null) {
            builder.slowCallDurationThreshold(parseDuration(model.getSlowCallDurationThreshold()));
        }

        if (model.getSlowCallRateThreshold() != null) {
            builder.slowCallRateThreshold(model.getSlowCallRateThreshold().floatValue());
        }

        return builder.build();
    }

    /**
     * Build TimeLimiterConfig from model
     */
    public static TimeLimiterConfig buildTimeLimiterConfig(TimeLimiterConfigModel model) {
        if (model == null) {
            return TimeLimiterConfig.ofDefaults();
        }

        TimeLimiterConfig.Builder builder = TimeLimiterConfig.custom();

        if (model.getTimeoutDuration() != null) {
            builder.timeoutDuration(parseDuration(model.getTimeoutDuration()));
        }

        if (model.getCancelRunningFuture() != null) {
            builder.cancelRunningFuture(model.getCancelRunningFuture());
        }

        return builder.build();
    }

    /**
     * Build BulkheadConfig from model
     */
    public static BulkheadConfig buildBulkheadConfig(BulkheadConfigModel model) {
        if (model == null) {
            return BulkheadConfig.ofDefaults();
        }

        BulkheadConfig.Builder builder = BulkheadConfig.custom();

        if (model.getMaxConcurrentCalls() != null) {
            builder.maxConcurrentCalls(model.getMaxConcurrentCalls());
        }

        if (model.getMaxWaitDuration() != null) {
            builder.maxWaitDuration(parseDuration(model.getMaxWaitDuration()));
        }

        return builder.build();
    }

    /**
     * Build ThreadPoolBulkheadConfig from model
     */
    public static ThreadPoolBulkheadConfig buildThreadPoolBulkheadConfig(ThreadPoolBulkheadConfigModel model) {
        if (model == null) {
            return ThreadPoolBulkheadConfig.ofDefaults();
        }

        ThreadPoolBulkheadConfig.Builder builder = ThreadPoolBulkheadConfig.custom();

        if (model.getCoreThreadPoolSize() != null) {
            builder.coreThreadPoolSize(model.getCoreThreadPoolSize());
        }

        if (model.getMaxThreadPoolSize() != null) {
            builder.maxThreadPoolSize(model.getMaxThreadPoolSize());
        }

        if (model.getQueueCapacity() != null) {
            builder.queueCapacity(model.getQueueCapacity());
        }

        if (model.getKeepAliveDuration() != null) {
            builder.keepAliveDuration(parseDuration(model.getKeepAliveDuration()));
        }

        return builder.build();
    }

    /**
     * Build RetryConfig from model
     */
    public static RetryConfig buildRetryConfig(RetryConfigModel model) {
        if (model == null) {
            return RetryConfig.ofDefaults();
        }

        RetryConfig.Builder<Object> builder = RetryConfig.custom();

        if (model.getMaxAttempts() != null) {
            builder.maxAttempts(model.getMaxAttempts());
        }

        if (model.getWaitDuration() != null) {
            builder.waitDuration(model.getWaitDuration());
        }

        if (model.getIntervalFunction() != null) {
            builder.intervalFunction(model.getIntervalFunction());
        }

        if (model.getIntervalBiFunction() != null) {
            builder.intervalBiFunction(model.getIntervalBiFunction());
        }

        if (model.getRetryOnResultPredicate() != null) {
            builder.retryOnResult(model.getRetryOnResultPredicate());
        }

        if (model.getRetryExceptionPredicate() != null) {
            builder.retryOnException(model.getRetryExceptionPredicate());
        }

        if (model.getRetryExceptions() != null && model.getRetryExceptions().length > 0) {
            builder.retryExceptions(model.getRetryExceptions());
        }

        if (model.getIgnoreExceptions() != null && model.getIgnoreExceptions().length > 0) {
            builder.ignoreExceptions(model.getIgnoreExceptions());
        }

        if (model.getFailAfterMaxAttempts() != null) {
            builder.failAfterMaxAttempts(model.getFailAfterMaxAttempts());
        }

        return builder.build();
    }

    /**
     * Build RateLimiterConfig from model
     */
    public static RateLimiterConfig buildRateLimiterConfig(RateLimiterConfigModel model) {
        if (model == null) {
            return RateLimiterConfig.ofDefaults();
        }

        RateLimiterConfig.Builder builder = RateLimiterConfig.custom();

        if (model.getLimitForPeriod() != null) {
            builder.limitForPeriod(model.getLimitForPeriod());
        }

        if (model.getLimitRefreshPeriod() != null) {
            builder.limitRefreshPeriod(parseDuration(model.getLimitRefreshPeriod()));
        }

        if (model.getTimeoutDuration() != null) {
            builder.timeoutDuration(parseDuration(model.getTimeoutDuration()));
        }

        return builder.build();
    }

    /**
     * Parse duration string to Duration object
     */
    private static Duration parseDuration(String value) {
        if (value == null || value.isEmpty()) {
            return Duration.ofSeconds(1);
        }

        try {
            if (value.matches("\\d+s")) {
                return Duration.ofSeconds(Long.parseLong(value.replace("s", "")));
            } else if (value.matches("\\d+ms")) {
                return Duration.ofMillis(Long.parseLong(value.replace("ms", "")));
            } else if (value.matches("\\d+m")) {
                return Duration.ofMinutes(Long.parseLong(value.replace("m", "")));
            } else {
                return Duration.parse(value);
            }
        } catch (Exception e) {
            return Duration.ofSeconds(1);
        }
    }
}
