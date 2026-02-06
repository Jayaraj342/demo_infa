package com.example.demo.resiliencetemp.core;

import com.example.demo.resiliencetemp.config.loader.ResilienceConfigLoader;
import com.example.demo.resiliencetemp.context.MDCContextPropagator;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ContextPropagator;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Thread-safe singleton registry using enum pattern that aggregates all Resilience4j registries
 * and provides convenient access to resilience components by name.
 *
 * Usage: ResiliencePolicyRegistry.INSTANCE.circuitBreaker("serviceName")
 */
public enum ResiliencePolicyRegistry {

    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(ResiliencePolicyRegistry.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
    private final RetryRegistry retryRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final ResilienceConfigLoader configLoader;

    private volatile List<? extends ContextPropagator> contextPropagators;
    private volatile boolean eventPublishersRegistered = false;

    ResiliencePolicyRegistry() {
        this.configLoader = new ResilienceConfigLoader();
        this.circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        this.timeLimiterRegistry = TimeLimiterRegistry.ofDefaults();
        this.bulkheadRegistry = BulkheadRegistry.ofDefaults();
        this.threadPoolBulkheadRegistry = ThreadPoolBulkheadRegistry.ofDefaults();
        this.retryRegistry = RetryRegistry.ofDefaults();
        this.rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
        initializeDefaultContextPropagators();
    }

    public List<? extends ContextPropagator> getContextPropagators() {
        return this.contextPropagators;
    }

    private void ensureEventPublishersRegistered() {
        if (!eventPublishersRegistered) {
            synchronized (this) {
                if (!eventPublishersRegistered) {
                    registerEventPublishers();
                    eventPublishersRegistered = true;
                }
            }
        }
    }

    private void registerEventPublishers() {
        // Add event listeners for Bulkhead monitoring
        bulkheadRegistry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    Bulkhead addedBulkhead = entryAddedEvent.getAddedEntry();
                    LOG.info("Bulkhead {} added", addedBulkhead.getName());
                    // Register individual bulkhead events
                    addedBulkhead.getEventPublisher()
                            .onCallPermitted(event -> LOG.debug("Bulkhead {} permitted call", addedBulkhead.getName()))
                            .onCallRejected(event -> LOG.warn("Bulkhead {} rejected call", addedBulkhead.getName()))
                            .onCallFinished(event -> LOG.debug("Bulkhead {} finished call", addedBulkhead.getName()));
                })
                .onEntryRemoved(entryRemovedEvent -> {
                    Bulkhead removedBulkhead = entryRemovedEvent.getRemovedEntry();
                    LOG.info("Bulkhead {} removed", removedBulkhead.getName());
                });

        // Add event listeners for ThreadPoolBulkhead monitoring
        threadPoolBulkheadRegistry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    ThreadPoolBulkhead addedBulkhead = entryAddedEvent.getAddedEntry();
                    LOG.info("ThreadPoolBulkhead {} added", addedBulkhead.getName());
                    // Register individual thread pool bulkhead events
                    addedBulkhead.getEventPublisher()
                            .onCallPermitted(event -> LOG.debug("ThreadPoolBulkhead {} permitted call", addedBulkhead.getName()))
                            .onCallRejected(event -> LOG.warn("ThreadPoolBulkhead {} rejected call", addedBulkhead.getName()))
                            .onCallFinished(event -> LOG.debug("ThreadPoolBulkhead {} finished call", addedBulkhead.getName()));
                })
                .onEntryRemoved(entryRemovedEvent -> {
                    ThreadPoolBulkhead removedBulkhead = entryRemovedEvent.getRemovedEntry();
                    LOG.info("ThreadPoolBulkhead {} removed", removedBulkhead.getName());
                });

        // Add event listeners for CircuitBreaker monitoring
        circuitBreakerRegistry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    CircuitBreaker addedCircuitBreaker = entryAddedEvent.getAddedEntry();
                    LOG.info("CircuitBreaker {} added", addedCircuitBreaker.getName());
                    // Register state transition and error events for the circuit breaker
                    addedCircuitBreaker.getEventPublisher()
                            .onStateTransition(event ->
                                    LOG.warn("Circuit breaker {} transitioned from {} to {}",
                                            addedCircuitBreaker.getName(),
                                            event.getStateTransition().getFromState(),
                                            event.getStateTransition().getToState()))
                            .onError(event ->
                                    LOG.error("Circuit breaker {} recorded error", addedCircuitBreaker.getName(), event.getThrowable()))
                            .onSuccess(event ->
                                    LOG.debug("Circuit breaker {} recorded success", addedCircuitBreaker.getName()))
                            .onCallNotPermitted(event ->
                                    LOG.warn("Circuit breaker {} rejected call (circuit open)", addedCircuitBreaker.getName()))
                            .onSlowCallRateExceeded(event ->
                                    LOG.warn("Circuit breaker {} slow call rate exceeded: {}%",
                                            addedCircuitBreaker.getName(), event.getSlowCallRate()))
                            .onFailureRateExceeded(event ->
                                    LOG.warn("Circuit breaker {} failure rate exceeded: {}%",
                                            addedCircuitBreaker.getName(), event.getFailureRate()));
                })
                .onEntryRemoved(entryRemovedEvent -> {
                    CircuitBreaker removedCircuitBreaker = entryRemovedEvent.getRemovedEntry();
                    LOG.info("CircuitBreaker {} removed", removedCircuitBreaker.getName());
                });

        // Add event listeners for Retry monitoring
        retryRegistry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    Retry addedRetry = entryAddedEvent.getAddedEntry();
                    LOG.info("Retry {} added", addedRetry.getName());
                    // Register retry events
                    addedRetry.getEventPublisher()
                            .onRetry(event -> LOG.warn("Retry {} attempt {} after {}ms",
                                    addedRetry.getName(),
                                    event.getNumberOfRetryAttempts(),
                                    event.getWaitInterval().toMillis()))
                            .onSuccess(event -> LOG.debug("Retry {} succeeded after {} attempts",
                                    addedRetry.getName(), event.getNumberOfRetryAttempts()))
                            .onError(event -> LOG.error("Retry {} failed after {} attempts",
                                    addedRetry.getName(), event.getNumberOfRetryAttempts(), event.getLastThrowable()));
                })
                .onEntryRemoved(entryRemovedEvent -> {
                    Retry removedRetry = entryRemovedEvent.getRemovedEntry();
                    LOG.info("Retry {} removed", removedRetry.getName());
                });

        // Add event listeners for RateLimiter monitoring
        rateLimiterRegistry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    RateLimiter addedRateLimiter = entryAddedEvent.getAddedEntry();
                    LOG.info("RateLimiter {} added", addedRateLimiter.getName());
                    // Register rate limiter events
                    addedRateLimiter.getEventPublisher()
                            .onSuccess(event -> LOG.debug("RateLimiter {} permitted call", addedRateLimiter.getName()))
                            .onFailure(event -> LOG.warn("RateLimiter {} rejected call (limit exceeded)", addedRateLimiter.getName()));
                })
                .onEntryRemoved(entryRemovedEvent -> {
                    RateLimiter removedRateLimiter = entryRemovedEvent.getRemovedEntry();
                    LOG.info("RateLimiter {} removed", removedRateLimiter.getName());
                });

        // Add event listeners for TimeLimiter monitoring
        timeLimiterRegistry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    TimeLimiter addedTimeLimiter = entryAddedEvent.getAddedEntry();
                    LOG.info("TimeLimiter {} added", addedTimeLimiter.getName());
                    // Register time limiter events
                    addedTimeLimiter.getEventPublisher()
                            .onSuccess(event -> LOG.debug("TimeLimiter {} call completed within timeout", addedTimeLimiter.getName()))
                            .onError(event -> LOG.error("TimeLimiter {} call failed", addedTimeLimiter.getName(), event.getThrowable()))
                            .onTimeout(event -> LOG.warn("TimeLimiter {} call timed out", addedTimeLimiter.getName()));
                })
                .onEntryRemoved(entryRemovedEvent -> {
                    TimeLimiter removedTimeLimiter = entryRemovedEvent.getRemovedEntry();
                    LOG.info("TimeLimiter {} removed", removedTimeLimiter.getName());
                });
    }

    /**
     * Get CircuitBreaker by name (e.g., "lics", "saas", "automapper", "frs")
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public CircuitBreaker circuitBreaker(String name) {
        ensureEventPublishersRegistered();
        CircuitBreakerConfig config;

        if (configLoader.hasConfig(name, "circuitbreaker")) {
            LOG.info("Loading CircuitBreaker configuration from YML for service: {}", name);
            config = buildCircuitBreakerConfig(name);
        } else {
            LOG.info("No CircuitBreaker configuration found in YML for service: {}, using defaults", name);
            config = CircuitBreakerConfig.ofDefaults();
        }

        return circuitBreakerRegistry.circuitBreaker(name, config);
    }

    private CircuitBreakerConfig buildCircuitBreakerConfig(String serviceName) {
        java.util.Map<String, Object> configMap = configLoader.getCircuitBreakerConfig(serviceName);

        CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom();

        // slidingWindowType: Configures the type of the sliding window (COUNT_BASED or TIME_BASED)
        // Default: COUNT_BASED
        if (configMap.containsKey("slidingWindowType")) {
            String slidingWindowType = ResilienceConfigLoader.getString(configMap, "slidingWindowType", null);
            if (slidingWindowType != null) {
                if ("TIME_BASED".equalsIgnoreCase(slidingWindowType)) {
                    builder.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED);
                } else if ("COUNT_BASED".equalsIgnoreCase(slidingWindowType)) {
                    builder.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED);
                } else {
                    throw new IllegalArgumentException("Invalid slidingWindowType: " + slidingWindowType);
                }
            }
        }

        // slidingWindowSize: Configures the size of the sliding window
        // Default: 100
        // For COUNT_BASED: last N calls recorded and aggregated
        // For TIME_BASED: calls of the last N seconds recorded and aggregated
        if (configMap.containsKey("slidingWindowSize")) {
            Integer slidingWindowSize = ResilienceConfigLoader.getInteger(configMap, "slidingWindowSize", null);
            if (slidingWindowSize != null) {
                builder.slidingWindowSize(slidingWindowSize);
            }
        }

        // minimumNumberOfCalls: Configures the minimum number of calls required before calculating error rate
        // Default: 100
        // If minimumNumberOfCalls is not reached, the CircuitBreaker will not transition to open
        if (configMap.containsKey("minimumNumberOfCalls")) {
            Integer minimumNumberOfCalls = ResilienceConfigLoader.getInteger(configMap, "minimumNumberOfCalls", null);
            if (minimumNumberOfCalls != null) {
                builder.minimumNumberOfCalls(minimumNumberOfCalls);
            }
        }

        // failureRateThreshold: Configures the failure rate threshold in percentage (0-100)
        // Default: 50
        // When failure rate >= threshold, CircuitBreaker transitions to open
        if (configMap.containsKey("failureRateThreshold")) {
            Double failureRateThreshold = ResilienceConfigLoader.getDouble(configMap, "failureRateThreshold", null);
            if (failureRateThreshold != null) {
                builder.failureRateThreshold(failureRateThreshold.floatValue());
            }
        }

        // slowCallRateThreshold: Configures the threshold in percentage for slow calls (0-100)
        // Default: 100
        // When percentage of slow calls >= threshold, CircuitBreaker transitions to open
        // A call is considered slow if duration > slowCallDurationThreshold
        if (configMap.containsKey("slowCallRateThreshold")) {
            Double slowCallRateThreshold = ResilienceConfigLoader.getDouble(configMap, "slowCallRateThreshold", null);
            if (slowCallRateThreshold != null) {
                builder.slowCallRateThreshold(slowCallRateThreshold.floatValue());
            }
        }

        // slowCallDurationThreshold: Configures the duration threshold above which calls are considered slow
        // Default: 60000 [ms]
        if (configMap.containsKey("slowCallDurationThreshold")) {
            java.time.Duration slowCallDurationThreshold = ResilienceConfigLoader.parseDuration(configMap.get("slowCallDurationThreshold"), null);
            if (slowCallDurationThreshold != null) {
                builder.slowCallDurationThreshold(slowCallDurationThreshold);
            }
        }

        // waitDurationInOpenState: The time that the CircuitBreaker should wait before transitioning from open to half-open
        // Default: 60000 [ms]
        if (configMap.containsKey("waitDurationInOpenState")) {
            java.time.Duration waitDuration = ResilienceConfigLoader.parseDuration(configMap.get("waitDurationInOpenState"), null);
            if (waitDuration != null) {
                builder.waitDurationInOpenState(waitDuration);
            }
        }

        // permittedNumberOfCallsInHalfOpenState: Configures the number of permitted calls when CircuitBreaker is half open
        // Default: 10
        if (configMap.containsKey("permittedNumberOfCallsInHalfOpenState")) {
            Integer permittedCalls = ResilienceConfigLoader.getInteger(configMap, "permittedNumberOfCallsInHalfOpenState", null);
            if (permittedCalls != null) {
                builder.permittedNumberOfCallsInHalfOpenState(permittedCalls);
            }
        }

        // maxWaitDurationInHalfOpenState: Configures the maximum wait duration in Half Open state before switching to open
        // Default: 0 [ms] (wait infinitely until all permitted calls are completed)
        if (configMap.containsKey("maxWaitDurationInHalfOpenState")) {
            java.time.Duration maxWaitDuration = ResilienceConfigLoader.parseDuration(configMap.get("maxWaitDurationInHalfOpenState"), null);
            if (maxWaitDuration != null) {
                builder.maxWaitDurationInHalfOpenState(maxWaitDuration);
            }
        }

        // automaticTransitionFromOpenToHalfOpenEnabled: If true, CircuitBreaker automatically transitions from open to half-open
        // Default: false
        // If false, transition only happens when a call is made
        if (configMap.containsKey("automaticTransitionFromOpenToHalfOpenEnabled")) {
            Boolean autoTransition = ResilienceConfigLoader.getBoolean(configMap, "automaticTransitionFromOpenToHalfOpenEnabled", null);
            if (autoTransition != null) {
                builder.automaticTransitionFromOpenToHalfOpenEnabled(autoTransition);
            }
        }

        // recordExceptions: Configures a list of Throwable classes that are recorded as a failure
        // Default: empty
        // Any exception matching or inheriting from list counts as failure, unless ignored via ignoreExceptions
        if (configMap.containsKey("recordExceptions")) {
            List<String> recordExceptionClassNames = getStringList(configMap, "recordExceptions");
            if (!recordExceptionClassNames.isEmpty()) {
                Class<? extends Throwable>[] exceptionClasses = loadExceptionClasses(recordExceptionClassNames);
                if (exceptionClasses.length > 0) {
                    builder.recordExceptions(exceptionClasses);
                }
            }
        }

        // ignoreExceptions: Configures a list of Throwable classes that are ignored
        // Default: empty
        // Neither count as failure nor success
        if (configMap.containsKey("ignoreExceptions")) {
            List<String> ignoreExceptionClassNames = getStringList(configMap, "ignoreExceptions");
            if (!ignoreExceptionClassNames.isEmpty()) {
                Class<? extends Throwable>[] exceptionClasses = loadExceptionClasses(ignoreExceptionClassNames);
                if (exceptionClasses.length > 0) {
                    builder.ignoreExceptions(exceptionClasses);
                }
            }
        }

        // recordFailurePredicate: Custom Predicate to evaluate if an exception should be recorded as a failure
        // Default: throwable -> true (record all exceptions)
        // Note: This is typically configured programmatically as it requires code

        // ignoreExceptionPredicate: Custom Predicate to evaluate if an exception should be ignored
        // Default: throwable -> false (ignore no exceptions)
        // Note: This is typically configured programmatically as it requires code

        return builder.build();
    }

    /**
     * Get TimeLimiter by name
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public TimeLimiter timeLimiter(String name) {
        TimeLimiterConfig config;

        if (configLoader.hasConfig(name, "timelimiter")) {
            LOG.info("Loading TimeLimiter configuration from YML for service: {}", name);
            config = buildTimeLimiterConfig(name);
        } else {
            LOG.info("No TimeLimiter configuration found in YML for service: {}, using defaults", name);
            config = TimeLimiterConfig.ofDefaults();
        }

        return timeLimiterRegistry.timeLimiter(name, config);
    }

    private TimeLimiterConfig buildTimeLimiterConfig(String serviceName) {
        java.util.Map<String, Object> configMap = configLoader.getTimeLimiterConfig(serviceName);

        TimeLimiterConfig.Builder builder = TimeLimiterConfig.custom();

        // Only set values that are explicitly present in YML
        if (configMap.containsKey("timeoutDuration")) {
            java.time.Duration timeoutDuration = ResilienceConfigLoader.parseDuration(configMap.get("timeoutDuration"), null);
            if (timeoutDuration != null) {
                builder.timeoutDuration(timeoutDuration);
            }
        }

        if (configMap.containsKey("cancelRunningFuture")) {
            Boolean cancelRunningFuture = ResilienceConfigLoader.getBoolean(configMap, "cancelRunningFuture", null);
            if (cancelRunningFuture != null) {
                builder.cancelRunningFuture(cancelRunningFuture);
            }
        }

        return builder.build();
    }

    /**
     * Get Bulkhead (semaphore-based) by name
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public Bulkhead bulkhead(String name) {
        BulkheadConfig config;

        if (configLoader.hasConfig(name, "bulkhead")) {
            LOG.info("Loading Bulkhead configuration from YML for service: {}", name);
            config = buildBulkheadConfig(name);
        } else {
            LOG.info("No Bulkhead configuration found in YML for service: {}, using defaults", name);
            config = BulkheadConfig.ofDefaults();
        }

        return bulkheadRegistry.bulkhead(name, config);
    }

    private BulkheadConfig buildBulkheadConfig(String serviceName) {
        java.util.Map<String, Object> configMap = configLoader.getBulkheadConfig(serviceName);

        BulkheadConfig.Builder builder = BulkheadConfig.custom();

        // Only set values that are explicitly present in YML
        if (configMap.containsKey("maxConcurrentCalls")) {
            Integer maxConcurrentCalls = ResilienceConfigLoader.getInteger(configMap, "maxConcurrentCalls", null);
            if (maxConcurrentCalls != null) {
                builder.maxConcurrentCalls(maxConcurrentCalls);
            }
        }

        if (configMap.containsKey("maxWaitDuration")) {
            java.time.Duration maxWaitDuration = ResilienceConfigLoader.parseDuration(configMap.get("maxWaitDuration"), null);
            if (maxWaitDuration != null) {
                builder.maxWaitDuration(maxWaitDuration);
            }
        }

        return builder.build();
    }

    /**
     * Get ThreadPoolBulkhead by name
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public ThreadPoolBulkhead threadPoolBulkhead(String name) {
        ThreadPoolBulkheadConfig config;

        if (configLoader.hasConfig(name, "threadpool_bulkhead")) {
            LOG.info("Loading ThreadPoolBulkhead configuration from YML for service: {}", name);
            config = buildThreadPoolBulkheadConfig(name);
        } else {
            LOG.info("No ThreadPoolBulkhead configuration found in YML for service: {}, using defaults", name);
            if (contextPropagators!= null && !contextPropagators.isEmpty()) {
                config = ThreadPoolBulkheadConfig.from(ThreadPoolBulkheadConfig.ofDefaults())
                        .contextPropagator(contextPropagators.toArray(new ContextPropagator[0]))
                        .build();
            } else {
                config = ThreadPoolBulkheadConfig.ofDefaults();
            }
        }

        return threadPoolBulkheadRegistry.bulkhead(name, config);
    }

    private ThreadPoolBulkheadConfig buildThreadPoolBulkheadConfig(String serviceName) {
        java.util.Map<String, Object> configMap = configLoader.getThreadPoolBulkheadConfig(serviceName);

        ThreadPoolBulkheadConfig.Builder builder = ThreadPoolBulkheadConfig.custom();

        // Only set values that are explicitly present in YML
        if (configMap.containsKey("coreThreadPoolSize")) {
            Integer coreThreadPoolSize = ResilienceConfigLoader.getInteger(configMap, "coreThreadPoolSize", null);
            if (coreThreadPoolSize != null) {
                builder.coreThreadPoolSize(coreThreadPoolSize);
            }
        }

        if (configMap.containsKey("maxThreadPoolSize")) {
            Integer maxThreadPoolSize = ResilienceConfigLoader.getInteger(configMap, "maxThreadPoolSize", null);
            if (maxThreadPoolSize != null) {
                builder.maxThreadPoolSize(maxThreadPoolSize);
            }
        }

        if (configMap.containsKey("queueCapacity")) {
            Integer queueCapacity = ResilienceConfigLoader.getInteger(configMap, "queueCapacity", null);
            if (queueCapacity != null) {
                builder.queueCapacity(queueCapacity);
            }
        }

        if (configMap.containsKey("keepAliveDuration")) {
            java.time.Duration keepAliveDuration = ResilienceConfigLoader.parseDuration(configMap.get("keepAliveDuration"), null);
            if (keepAliveDuration != null) {
                builder.keepAliveDuration(keepAliveDuration);
            }
        }

        if(contextPropagators != null && !contextPropagators.isEmpty()) {
            builder.contextPropagator(contextPropagators.toArray(new ContextPropagator[0]));
        }
        return builder.build();
    }

    /**
     * Get Retry by name
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public Retry retry(String name) {
        RetryConfig config;

        if (configLoader.hasConfig(name, "retry")) {
            LOG.info("Loading Retry configuration from YML for service: {}", name);
            config = buildRetryConfig(name);
        } else {
            LOG.info("No Retry configuration found in YML for service: {}, using defaults", name);
            config = RetryConfig.ofDefaults();
        }

        return retryRegistry.retry(name, config);
    }

    private RetryConfig buildRetryConfig(String serviceName) {
        java.util.Map<String, Object> configMap = configLoader.getRetryConfig(serviceName);

        RetryConfig.Builder<Object> builder = RetryConfig.custom();

        // maxAttempts: The maximum number of attempts (including the initial call as the first attempt)
        // Default: 3
        if (configMap.containsKey("maxAttempts")) {
            Integer maxAttempts = ResilienceConfigLoader.getInteger(configMap, "maxAttempts", null);
            if (maxAttempts != null) {
                builder.maxAttempts(maxAttempts);
            }
        }

        // waitDuration: A fixed wait duration between retry attempts
        // Default: 500 [ms]
        // intervalFunction: A function to modify the waiting interval after a failure
        // Can be configured as "exponential", "exponentialRandomized", or combined via flags
        java.time.Duration waitDuration = ResilienceConfigLoader.parseDuration(configMap.get("waitDuration"), null);
        Boolean enableExponentialBackoff = ResilienceConfigLoader.getBoolean(configMap, "enableExponentialBackoff", false);
        Boolean enableRandomizedWait = ResilienceConfigLoader.getBoolean(configMap, "enableRandomizedWait", false);

        if (waitDuration != null && waitDuration.toMillis() >= 0) {
            if (Boolean.TRUE.equals(enableExponentialBackoff) && Boolean.TRUE.equals(enableRandomizedWait)) {
                configureExponentialBackoffAndRandomizedWait(configMap, builder, waitDuration);
            } else if (Boolean.TRUE.equals(enableExponentialBackoff)) {
                configureExponentialBackoff(configMap, builder, waitDuration);
            } else if (Boolean.TRUE.equals(enableRandomizedWait)) {
                configureRandomizedWait(configMap, builder, waitDuration);
            } else {
                builder.waitDuration(waitDuration);
            }
        }

        // retryExceptions: Configures a list of Throwable classes that are recorded as a failure and thus are retried
        // This parameter supports subtyping
        // Default: empty
        if (configMap.containsKey("retryExceptions")) {
            List<String> retryExceptionClassNames = getStringList(configMap, "retryExceptions");
            if (!retryExceptionClassNames.isEmpty()) {
                Class<? extends Throwable>[] exceptionClasses = loadExceptionClasses(retryExceptionClassNames);
                if (exceptionClasses.length > 0) {
                    builder.retryExceptions(exceptionClasses);
                }
            }
        }

        // ignoreExceptions: Configures a list of Throwable classes that are ignored and thus are not retried
        // This parameter supports subtyping
        // Default: empty
        if (configMap.containsKey("ignoreExceptions")) {
            List<String> ignoreExceptionClassNames = getStringList(configMap, "ignoreExceptions");
            if (!ignoreExceptionClassNames.isEmpty()) {
                Class<? extends Throwable>[] exceptionClasses = loadExceptionClasses(ignoreExceptionClassNames);
                if (exceptionClasses.length > 0) {
                    builder.ignoreExceptions(exceptionClasses);
                }
            }
        }

        // failAfterMaxAttempts: A boolean to enable or disable throwing of MaxRetriesExceededException
        // when the Retry has reached the configured maxAttempts, and the result is still not passing the retryOnResultPredicate
        // Default: false
        if (configMap.containsKey("failAfterMaxAttempts")) {
            Boolean failAfterMaxAttempts = ResilienceConfigLoader.getBoolean(configMap, "failAfterMaxAttempts", null);
            if (failAfterMaxAttempts != null) {
                builder.failAfterMaxAttempts(failAfterMaxAttempts);
            }
        }

        return builder.build();
    }

    /**
     * Configure exponential backoff with randomization.
     * Supports optional multiplier and randomization factor from config.
     *
     * @param configMap configuration map
     * @param builder retry config builder
     * @param waitDuration base wait duration
     */
    private void configureExponentialBackoffAndRandomizedWait(java.util.Map<String, Object> configMap,
                                                                RetryConfig.Builder<Object> builder,
                                                                java.time.Duration waitDuration) {
        Double multiplier = ResilienceConfigLoader.getDouble(configMap, "exponentialBackoffMultiplier", 2.0);
        Double randomizationFactor = ResilienceConfigLoader.getDouble(configMap, "randomizationFactor", 0.5);

        builder.intervalFunction(
                io.github.resilience4j.core.IntervalFunction.ofExponentialRandomBackoff(
                        waitDuration,
                        multiplier,
                        randomizationFactor
                )
        );
    }

    /**
     * Configure exponential backoff without randomization.
     * Supports optional multiplier from config.
     *
     * @param configMap configuration map
     * @param builder retry config builder
     * @param waitDuration base wait duration
     */
    private void configureExponentialBackoff(java.util.Map<String, Object> configMap,
                                              RetryConfig.Builder<Object> builder,
                                              java.time.Duration waitDuration) {
        Double multiplier = ResilienceConfigLoader.getDouble(configMap, "exponentialBackoffMultiplier", 2.0);

        builder.intervalFunction(
                io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                        waitDuration,
                        multiplier
                )
        );
    }

    /**
     * Configure randomized wait without exponential backoff.
     * Supports optional randomization factor from config.
     *
     * @param configMap configuration map
     * @param builder retry config builder
     * @param waitDuration base wait duration
     */
    private void configureRandomizedWait(java.util.Map<String, Object> configMap,
                                         RetryConfig.Builder<Object> builder,
                                         java.time.Duration waitDuration) {
        Double randomizationFactor = ResilienceConfigLoader.getDouble(configMap, "randomizationFactor", 0.5);

        builder.intervalFunction(
                io.github.resilience4j.core.IntervalFunction.ofRandomized(
                        waitDuration,
                        randomizationFactor
                )
        );
    }

    /**
     * Helper method to get a list of strings from configuration map
     */
    @SuppressWarnings("unchecked")
    private List<String> getStringList(java.util.Map<String, Object> configMap, String key) {
        Object value = configMap.get(key);
        if (value == null) {
            return java.util.Collections.emptyList();
        }

        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        } else if (value instanceof String) {
            // Support comma-separated strings
            String[] items = ((String) value).split(",");
            List<String> result = new ArrayList<>();
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        }

        return java.util.Collections.emptyList();
    }

    /**
     * Helper method to load exception classes from their fully qualified names
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Throwable>[] loadExceptionClasses(List<String> classNames) {
        List<Class<? extends Throwable>> classes = new ArrayList<>();

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (Throwable.class.isAssignableFrom(clazz)) {
                    classes.add((Class<? extends Throwable>) clazz);
                } else {
                    LOG.warn("Class {} is not a Throwable, skipping", className);
                }
            } catch (ClassNotFoundException e) {
                LOG.warn("Exception class {} not found, skipping", className, e);
            }
        }

        return classes.toArray(new Class[0]);
    }

    /**
     * Get RateLimiter by name
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public RateLimiter rateLimiter(String name) {
        RateLimiterConfig config;

        if (configLoader.hasConfig(name, "ratelimiter")) {
            LOG.info("Loading RateLimiter configuration from YML for service: {}", name);
            config = buildRateLimiterConfig(name);
        } else {
            LOG.info("No RateLimiter configuration found in YML for service: {}, using defaults", name);
            config = RateLimiterConfig.ofDefaults();
        }

        return rateLimiterRegistry.rateLimiter(name, config);
    }

    private RateLimiterConfig buildRateLimiterConfig(String serviceName) {
        java.util.Map<String, Object> configMap = configLoader.getRateLimiterConfig(serviceName);

        RateLimiterConfig.Builder builder = RateLimiterConfig.custom();

        // Only set values that are explicitly present in YML
        if (configMap.containsKey("limitForPeriod")) {
            Integer limitForPeriod = ResilienceConfigLoader.getInteger(configMap, "limitForPeriod", null);
            if (limitForPeriod != null) {
                builder.limitForPeriod(limitForPeriod);
            }
        }

        if (configMap.containsKey("limitRefreshPeriod")) {
            java.time.Duration limitRefreshPeriod = ResilienceConfigLoader.parseDuration(configMap.get("limitRefreshPeriod"), null);
            if (limitRefreshPeriod != null) {
                builder.limitRefreshPeriod(limitRefreshPeriod);
            }
        }

        if (configMap.containsKey("timeoutDuration")) {
            java.time.Duration timeoutDuration = ResilienceConfigLoader.parseDuration(configMap.get("timeoutDuration"), null);
            if (timeoutDuration != null) {
                builder.timeoutDuration(timeoutDuration);
            }
        }

        return builder.build();
    }

    public CircuitBreaker circuitBreaker(String name, CircuitBreakerConfig config) {
        CircuitBreaker circuitBreaker = CircuitBreaker.of(name, config);
        circuitBreakerRegistry.replace(name, circuitBreaker);
        return circuitBreaker;
    }

    public TimeLimiter timeLimiter(String name, TimeLimiterConfig config) {
        TimeLimiter timeLimiter = TimeLimiter.of(name, config);
        timeLimiterRegistry.replace(name, timeLimiter);
        return timeLimiter;
    }

    public Bulkhead bulkhead(String name, BulkheadConfig config) {
        Bulkhead bulkhead = Bulkhead.of(name, config);
        bulkheadRegistry.replace(name, bulkhead);
        return bulkhead;
    }

    public ThreadPoolBulkhead threadPoolBulkhead(String name, ThreadPoolBulkheadConfig config) {
        ThreadPoolBulkhead threadPoolBulkhead = ThreadPoolBulkhead.of(name, config);
        threadPoolBulkheadRegistry.replace(name, threadPoolBulkhead);
        return threadPoolBulkhead;
    }

    public Retry retry(String name, RetryConfig config) {
        Retry retry = Retry.of(name, config);
        retryRegistry.replace(name, retry);
        return retry;
    }

    public RateLimiter rateLimiter(String name, RateLimiterConfig config) {
        RateLimiter rateLimiter = RateLimiter.of(name, config);
        rateLimiterRegistry.replace(name, rateLimiter);
        return rateLimiter;
    }

    /**
     * Initialize context propagators with the default set (MDC and Ctx).
     * This method replaces any existing propagators with the default ones.
     */
    public void initializeDefaultContextPropagators() {
        this.contextPropagators = Arrays.asList(
                new MDCContextPropagator()
        );
    }

    /**
     * Register and append custom context propagators to the existing list.
     * If no propagators are currently registered, this will set the provided ones.
     * If propagators already exist, the new ones will be appended to the list.
     * Thread-safe method for thread-pool bulkhead context propagation.
     *
     * @param contextPropagators varargs array of ContextPropagator instances to register
     */
    public synchronized void appendContextPropagators(ContextPropagator<?>... contextPropagators) {
        ArrayList newPropagators = new ArrayList<>(this.contextPropagators);
        newPropagators.addAll(Arrays.stream(contextPropagators).toList());
        this.contextPropagators = newPropagators;
    }
}
