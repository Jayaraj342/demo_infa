package com.example.demo.resilience.util;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import java.util.function.Supplier;

/**
 * Generic client for invoking service operations with resilience patterns.
 * @see ResiliencePolicyRegistry
 * @see ResilientDecorator
 */
public class ResilientServiceClient {

    private final ResiliencePolicyRegistry registry;

    public ResilientServiceClient(ResiliencePolicyRegistry registry) {
        this.registry = registry;
    }

    /**
     * Execute a supplier with thread pool isolation (similar to Hystrix THREAD isolation).
     * Applies CircuitBreaker, ThreadPoolBulkhead, and TimeLimiter.
     *
     * @param supplier The operation to execute
     * @param serviceName The name of the service (used to lookup resilience policies)
     * @param <T> The return type
     * @return The result of the operation
     */
    public <T> T executeWithThreadPool(Supplier<T> supplier, String serviceName) {
        return ResilientDecorator.of(supplier)
                .withCircuitBreaker(registry.circuitBreaker(serviceName))
                .withThreadPoolBulkhead(registry.threadPoolBulkhead(serviceName))
                .withTimeLimiter(registry.timeLimiter(serviceName))
                .execute();
    }

    /**
     * Execute a supplier with semaphore isolation (similar to Hystrix SEMAPHORE isolation).
     * Applies CircuitBreaker, Bulkhead (semaphore-based), and TimeLimiter.
     *
     * @param supplier The operation to execute
     * @param serviceName The name of the service (used to lookup resilience policies)
     * @param <T> The return type
     * @return The result of the operation
     */
    public <T> T executeWithSemaphore(Supplier<T> supplier, String serviceName) {
        return ResilientDecorator.of(supplier)
                .withCircuitBreaker(registry.circuitBreaker(serviceName))
                .withBulkhead(registry.bulkhead(serviceName))
                .withTimeLimiter(registry.timeLimiter(serviceName))
                .execute();
    }

    /**
     * Execute with thread pool isolation and custom fallback.
     *
     * @param supplier The operation to execute
     * @param serviceName The name of the service
     * @param fallback The fallback operation if the main operation fails
     * @param <T> The return type
     * @return The result of the operation or fallback
     */
    public <T> T executeWithThreadPoolAndFallback(Supplier<T> supplier, String serviceName, Supplier<T> fallback) {
        return ResilientDecorator.of(supplier)
                .withCircuitBreaker(registry.circuitBreaker(serviceName))
                .withThreadPoolBulkhead(registry.threadPoolBulkhead(serviceName))
                .withTimeLimiter(registry.timeLimiter(serviceName))
                .withFallback(fallback)
                .execute();
    }

    /**
     * Execute with semaphore isolation and custom fallback.
     *
     * @param supplier The operation to execute
     * @param serviceName The name of the service
     * @param fallback The fallback operation if the main operation fails
     * @param <T> The return type
     * @return The result of the operation or fallback
     */
    public <T> T executeWithSemaphoreAndFallback(Supplier<T> supplier, String serviceName, Supplier<T> fallback) {
        return ResilientDecorator.of(supplier)
                .withCircuitBreaker(registry.circuitBreaker(serviceName))
                .withBulkhead(registry.bulkhead(serviceName))
                .withTimeLimiter(registry.timeLimiter(serviceName))
                .withFallback(fallback)
                .execute();
    }

    /**
     * Builder-style API for more complex scenarios.
     *
     * @param supplier The operation to execute
     * @param <T> The return type
     * @return A builder for configuring resilience patterns
     */
    public <T> ResilienceBuilder<T> custom(Supplier<T> supplier, String serviceName) {
        return new ResilienceBuilder<>(supplier, serviceName, registry);
    }

    /**
     * Fluent builder for custom resilience configurations.
     */
    public static class ResilienceBuilder<T> {
        private final Supplier<T> supplier;
        private final ResiliencePolicyRegistry registry;
        private String serviceName;
        private boolean useThreadPool = false;
        private boolean useSemaphore = false;
        private boolean useCircuitBreaker = false;
        private boolean useTimeLimiter = false;
        private boolean useRetry = false;
        private boolean useRateLimiter = false;

        private ThreadPoolBulkheadConfig threadPoolConfig;
        private BulkheadConfig bulkheadConfig;
        private CircuitBreakerConfig circuitBreakerConfig;
        private TimeLimiterConfig timeLimiterConfig;
        private RetryConfig retryConfig;
        private RateLimiterConfig rateLimiterConfig;

        private Supplier<T> fallback;

        ResilienceBuilder(Supplier<T> supplier, String serviceName, ResiliencePolicyRegistry registry) {
            this.supplier = supplier;
            this.serviceName = serviceName;
            this.registry = registry;
        }

        public ResilienceBuilder<T> withThreadPoolIsolation() {
            this.useThreadPool = true;
            this.useSemaphore = false;
            return this;
        }

//        public ResilienceBuilder<T> withThreadPoolIsolation(ThreadPoolBulkheadConfigModel threadPoolConfig) {
//            this.useThreadPool = true;
//            this.useSemaphore = false;
//            this.threadPoolConfig = ResilienceConfigBuilder.buildThreadPoolBulkheadConfig(threadPoolConfig);
//            this.serviceName = "temp";
//            return this;
//        }
//
//        public ResilienceBuilder<T> withSemaphoreIsolation() {
//            this.useSemaphore = true;
//            this.useThreadPool = false;
//            return this;
//        }
//        public ResilienceBuilder<T> withSemaphoreIsolation(BulkheadConfigModel bulkheadConfig) {
//            this.useSemaphore = true;
//            this.useThreadPool = false;
//            this.bulkheadConfig = ResilienceConfigBuilder.buildBulkheadConfig(bulkheadConfig);
//            this.serviceName = "temp";
//            return this;
//        }
//
//        public ResilienceBuilder<T> withCircuitBreaker() {
//            this.useCircuitBreaker = true;
//            return this;
//        }
//        public ResilienceBuilder<T> withCircuitBreaker(CircuitBreakerConfigModel circuitBreakerConfig) {
//            this.useCircuitBreaker = true;
//            this.circuitBreakerConfig = ResilienceConfigBuilder.buildCircuitBreakerConfig(circuitBreakerConfig);
//            this.serviceName = "temp";
//            return this;
//        }
//
//        public ResilienceBuilder<T> withTimeLimiter() {
//            this.useTimeLimiter = true;
//            return this;
//        }
//
//        public ResilienceBuilder<T> withTimeLimiter(TimeLimiterConfigModel timeLimiterConfig) {
//            this.useTimeLimiter = true;
//            this.timeLimiterConfig = ResilienceConfigBuilder.buildTimeLimiterConfig(timeLimiterConfig);
//            this.serviceName = "temp";
//            return this;
//        }
//
//        public ResilienceBuilder<T> withRetry() {
//            this.useRetry = true;
//            return this;
//        }
//
//        public ResilienceBuilder<T> withRetry(RetryConfigModel retryConfig) {
//            this.useRetry = true;
//            this.retryConfig = ResilienceConfigBuilder.buildRetryConfig(retryConfig);
//            this.serviceName = "temp";
//            return this;
//        }
//
//        public ResilienceBuilder<T> withRateLimiter() {
//            this.useRateLimiter = true;
//            return this;
//        }
//
//        public ResilienceBuilder<T> withRateLimiter(RateLimiterConfigModel rateLimiterConfig) {
//            this.useRateLimiter = true;
//            this.rateLimiterConfig = ResilienceConfigBuilder.buildRateLimiterConfig(rateLimiterConfig);
//            this.serviceName = "temp";
//            return this;
//        }
//
//        public ResilienceBuilder<T> withFallback(Supplier<T> fallback) {
//            this.fallback = fallback;
//            return this;
//        }
//
        public T execute() {
            ResilientDecorator.SupplierDecorator<T> decorator = ResilientDecorator.of(supplier);
//
//            if (useCircuitBreaker) {
//                CircuitBreaker circuitBreaker;
//                circuitBreaker = circuitBreakerConfig != null ? registry.circuitBreaker(serviceName, circuitBreakerConfig) : registry.circuitBreaker(serviceName);
//                decorator = decorator.withCircuitBreaker(circuitBreaker);
//            }
//
//            if (useThreadPool) {
//                ThreadPoolBulkhead threadPoolBulkhead = threadPoolConfig != null ? registry.threadPoolBulkhead(serviceName, threadPoolConfig) : registry.threadPoolBulkhead(serviceName);
//                decorator = decorator.withThreadPoolBulkhead(threadPoolBulkhead);
//            } else if (useSemaphore) {
//                Bulkhead bulkhead = bulkheadConfig != null ? registry.bulkhead(serviceName, bulkheadConfig) : registry.bulkhead(serviceName);
//                decorator = decorator.withBulkhead(bulkhead);
//            }
//
//            if (useTimeLimiter) {
//                TimeLimiter timeLimiter = timeLimiterConfig != null ? registry.timeLimiter(serviceName, timeLimiterConfig) : registry.timeLimiter(serviceName);
//                decorator = decorator.withTimeLimiter(timeLimiter);
//            }
//
//            if (useRetry) {
//                Retry retry = retryConfig != null ? registry.retry(serviceName, retryConfig) : registry.retry(serviceName);
//                decorator = decorator.withRetry(retry);
//            }
//
//            if (useRateLimiter) {
//                RateLimiter rateLimiter = rateLimiterConfig != null ? registry.rateLimiter(serviceName, rateLimiterConfig) : registry.rateLimiter(serviceName);
//                decorator = decorator.withRateLimiter(rateLimiter);
//            }
//
//            if (fallback != null) {
//                decorator = decorator.withFallback(fallback);
//            }

            return decorator.execute();
        }
    }
}
