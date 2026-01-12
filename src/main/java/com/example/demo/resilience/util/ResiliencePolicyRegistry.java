package com.example.demo.resilience.util;

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

import static java.util.stream.Collectors.toList;

/**
 * Thread-safe singleton registry using enum pattern that aggregates all Resilience4j registries
 * and provides convenient access to resilience components by name.
 * <p>
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

    public List<? extends ContextPropagator> getContextPropagators() {
        return contextPropagators;
    }

    private volatile List<? extends ContextPropagator> contextPropagators;
    private volatile boolean eventPublishersRegistered = false;

    ResiliencePolicyRegistry() {
        this.circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        this.timeLimiterRegistry = TimeLimiterRegistry.ofDefaults();
        this.bulkheadRegistry = BulkheadRegistry.ofDefaults();
        this.threadPoolBulkheadRegistry = ThreadPoolBulkheadRegistry.ofDefaults();
        this.retryRegistry = RetryRegistry.ofDefaults();
        this.rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
        this.contextPropagators = new ArrayList<>();
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


        LOG.info("No CircuitBreaker configuration found in YML for service: {}, using defaults", name);
        config = CircuitBreakerConfig.ofDefaults();

        return circuitBreakerRegistry.circuitBreaker(name, config);
    }

    private CircuitBreakerConfig buildCircuitBreakerConfig(String serviceName) {

        CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom();

        // Only set values that are explicitly present in YML

        builder.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED);


//        if (configMap.containsKey("slidingWindowSize")) {
//            Integer slidingWindowSize = ResilienceConfigLoader.getInteger(configMap, "slidingWindowSize", null);
//            if (slidingWindowSize != null) {
//                builder.slidingWindowSize(slidingWindowSize);
//            }
//        }
//
//        if (configMap.containsKey("minimumNumberOfCalls")) {
//            Integer minimumNumberOfCalls = ResilienceConfigLoader.getInteger(configMap, "minimumNumberOfCalls", null);
//            if (minimumNumberOfCalls != null) {
//                builder.minimumNumberOfCalls(minimumNumberOfCalls);
//            }
//        }
//
//        if (configMap.containsKey("failureRateThreshold")) {
//            Double failureRateThreshold = ResilienceConfigLoader.getDouble(configMap, "failureRateThreshold", null);
//            if (failureRateThreshold != null) {
//                builder.failureRateThreshold(failureRateThreshold.floatValue());
//            }
//        }
//
//        if (configMap.containsKey("waitDurationInOpenState")) {
//            java.time.Duration waitDuration = ResilienceConfigLoader.parseDuration(configMap.get("waitDurationInOpenState"), null);
//            if (waitDuration != null) {
//                builder.waitDurationInOpenState(waitDuration);
//            }
//        }
//
//        if (configMap.containsKey("permittedNumberOfCallsInHalfOpenState")) {
//            Integer permittedCalls = ResilienceConfigLoader.getInteger(configMap, "permittedNumberOfCallsInHalfOpenState", null);
//            if (permittedCalls != null) {
//                builder.permittedNumberOfCallsInHalfOpenState(permittedCalls);
//            }
//        }
//
//        if (configMap.containsKey("automaticTransitionFromOpenToHalfOpenEnabled")) {
//            Boolean autoTransition = ResilienceConfigLoader.getBoolean(configMap, "automaticTransitionFromOpenToHalfOpenEnabled", null);
//            if (autoTransition != null) {
//                builder.automaticTransitionFromOpenToHalfOpenEnabled(autoTransition);
//            }
//        }

        return builder.build();
    }

    /**
     * Get TimeLimiter by name
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public TimeLimiter timeLimiter(String name) {
        TimeLimiterConfig config;


        LOG.info("No TimeLimiter configuration found in YML for service: {}, using defaults", name);
        config = TimeLimiterConfig.ofDefaults();


        return timeLimiterRegistry.timeLimiter(name, config);
    }

    private TimeLimiterConfig buildTimeLimiterConfig(String serviceName) {

        TimeLimiterConfig.Builder builder = TimeLimiterConfig.custom();


        return builder.build();
    }

    /**
     * Get Bulkhead (semaphore-based) by name
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public Bulkhead bulkhead(String name) {
        BulkheadConfig config;

        LOG.info("No Bulkhead configuration found in YML for service: {}, using defaults", name);
        config = BulkheadConfig.ofDefaults();

        return bulkheadRegistry.bulkhead(name, config);
    }

    private BulkheadConfig buildBulkheadConfig(String serviceName) {

        BulkheadConfig.Builder builder = BulkheadConfig.custom();

        return builder.build();
    }

    /**
     * Get ThreadPoolBulkhead by name
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public ThreadPoolBulkhead threadPoolBulkhead(String name) {
        ThreadPoolBulkheadConfig config;


        LOG.info("No ThreadPoolBulkhead configuration found in YML for service: {}, using defaults", name);
        if (contextPropagators != null && !contextPropagators.isEmpty()) {
            config = ThreadPoolBulkheadConfig.from(ThreadPoolBulkheadConfig.ofDefaults())
                    .contextPropagator(contextPropagators.toArray(new ContextPropagator[0]))
                    .build();
        } else {
            config = ThreadPoolBulkheadConfig.ofDefaults();
        }

        return threadPoolBulkheadRegistry.bulkhead(name, config);
    }

    private ThreadPoolBulkheadConfig buildThreadPoolBulkheadConfig(String serviceName) {

        ThreadPoolBulkheadConfig.Builder builder = ThreadPoolBulkheadConfig.custom();

        if (contextPropagators != null && !contextPropagators.isEmpty()) {
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


        LOG.info("No Retry configuration found in YML for service: {}, using defaults", name);
        config = RetryConfig.ofDefaults();

        return retryRegistry.retry(name, config);
    }

    private RetryConfig buildRetryConfig(String serviceName) {

        RetryConfig.Builder builder = RetryConfig.custom();

        return builder.build();
    }

    /**
     * Get RateLimiter by name
     * Loads configuration from YML if available, otherwise uses defaults
     */
    public RateLimiter rateLimiter(String name) {
        RateLimiterConfig config;

        LOG.info("No RateLimiter configuration found in YML for service: {}, using defaults", name);
        config = RateLimiterConfig.ofDefaults();

        return rateLimiterRegistry.rateLimiter(name, config);
    }

    private RateLimiterConfig buildRateLimiterConfig(String serviceName) {

        RateLimiterConfig.Builder builder = RateLimiterConfig.custom();

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
     * Register context propagators for ThreadPoolBulkhead
     * Thread-safe method for updating context propagators
     */
    public synchronized void registerContextPropagator(ContextPropagator<?>... contextPropagators) {
        this.contextPropagators = Arrays.stream(contextPropagators).collect(toList());
    }

    /**
     * Get the singleton instance
     *
     * @return the singleton instance
     */
    public static ResiliencePolicyRegistry getInstance() {
        return INSTANCE;
    }
}
