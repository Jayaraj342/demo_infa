package com.example.demo.resiliencetemp.core;

import com.example.demo.resiliencetemp.config.model.RateLimiterConfigModel;
import com.example.demo.resiliencetemp.config.model.ResilienceConfigBuilder;
import com.example.demo.resiliencetemp.config.model.RetryConfigModel;
import com.example.demo.resiliencetemp.config.model.TimeLimiterConfigModel;
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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Generic client for invoking service operations with resilience patterns.
 * @see ResiliencePolicyRegistry
 * @see ResilientDecorator
 */
public class ResilientServiceClient {

        private final ResilienceBuilder builder;

    /**
     * Default constructor that creates a client with singleton registry instance.
     * Initializes default context propagators (MDC and Ctx) for thread-pool bulkhead.
     */
    private ResilientServiceClient(ResilienceBuilder builder) {
        this.builder = Objects.requireNonNull(builder);
    }

    /**
     * Builder-style API for more complex scenarios.
     *
     * @param supplier The operation to execute
     * @param serviceName The service name for policy lookup
     * @param <T> The return type
     * @return A builder for configuring resilience patterns
     */
    public static <T> ResilienceBuilder<T> builder(Supplier<T> supplier, String serviceName) {
        return new ResilienceBuilder<>(supplier, serviceName);
    }

    /**
     * Builder-style API without initial supplier.
     *
     * @param serviceName The service name for policy lookup
     * @param <T> The return type
     * @return A builder for configuring resilience patterns
     */
    public static <T> ResilienceBuilder<T> builder(String serviceName) {
        return new ResilienceBuilder<>(Objects.requireNonNull(serviceName));
    }

    /**
     * Execute the operation configured in the internal builder.
     * The builder must be initialized method before calling execute.
     *
     * @param <T> The return type
     * @return The result of the execution
     * @throws IllegalStateException if builder is not initialized or supplier is not provided
     */
    public <T> T execute() {
        return (T) this.builder.execute();
    }

    /**
     * Execute the operation with the provided supplier using the internal builder configuration.
     * The builder must be initialized method before calling execute.
     *
     * @param supplier The operation to execute
     * @param <T> The return type
     * @return The result of the execution
     * @throws IllegalStateException if builder is not initialized
     */
    public <T> T execute(Supplier<T> supplier) {
        return (T) this.builder.execute(supplier);
    }



    /**
     * Fluent builder for custom resilience configurations.
     */
    public static class ResilienceBuilder<T> {
        private Supplier<T> supplier;
        private final ResiliencePolicyRegistry registry = ResiliencePolicyRegistry.INSTANCE;
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

        private Function<Throwable, T> fallback;

        ResilienceBuilder(Supplier<T> supplier, String serviceName) {
            this.supplier = Objects.requireNonNull(supplier);
            this.serviceName = Objects.requireNonNull(serviceName);
        }

        ResilienceBuilder(String serviceName) {
            this.serviceName = Objects.requireNonNull(serviceName);
        }

        public ResilienceBuilder<T> withThreadPoolIsolation() {
            this.useThreadPool = true;
            this.useSemaphore = false;
            return this;
        }

        public ResilienceBuilder<T> withSemaphoreIsolation() {
            this.useSemaphore = true;
            this.useThreadPool = false;
            return this;
        }

        public ResilienceBuilder<T> withCircuitBreaker() {
            this.useCircuitBreaker = true;
            return this;
        }

        public ResilienceBuilder<T> withTimeLimiter() {
            this.useTimeLimiter = true;
            return this;
        }

        public ResilienceBuilder<T> withTimeLimiter(TimeLimiterConfigModel timeLimiterConfig) {
            this.useTimeLimiter = true;
            this.timeLimiterConfig = ResilienceConfigBuilder.buildTimeLimiterConfig(Objects.requireNonNull(timeLimiterConfig));
            return this;
        }

        public ResilienceBuilder<T> withRetry() {
            this.useRetry = true;
            return this;
        }

        public ResilienceBuilder<T> withRetry(RetryConfigModel retryConfig) {
            this.useRetry = true;
            this.retryConfig = ResilienceConfigBuilder.buildRetryConfig(retryConfig);
            return this;
        }

        public ResilienceBuilder<T> withRateLimiter() {
            this.useRateLimiter = true;
            return this;
        }

        public ResilienceBuilder<T> withRateLimiter(RateLimiterConfigModel rateLimiterConfig) {
            this.useRateLimiter = true;
            this.rateLimiterConfig = ResilienceConfigBuilder.buildRateLimiterConfig(rateLimiterConfig);
            return this;
        }

        public ResilienceBuilder<T> withFallback(Function<Throwable, T> fallback) {
            this.fallback = Objects.requireNonNull(fallback);
            return this;
        }

        /**
         * Builds and returns a ResilientServiceClient configured with this builder's settings.
         * 
         * @return A ResilientServiceClient instance ready for execution
         */
        public ResilientServiceClient build() {
            return new ResilientServiceClient(this);
        }

        public T execute() {
            if (this.supplier == null) {
                throw new IllegalStateException("Supplier function must be provided");
            }

            ResilientDecorator.SupplierDecorator<T> decorator = ResilientDecorator.of(this.supplier);

            if (this.useCircuitBreaker) {
                                CircuitBreaker circuitBreaker = this.circuitBreakerConfig != null ? CircuitBreaker.of(this.serviceName, this.circuitBreakerConfig) : this.registry.circuitBreaker(this.serviceName);
                decorator = decorator.withCircuitBreaker(circuitBreaker);
            }

            if (this.useThreadPool) {
                ThreadPoolBulkhead threadPoolBulkhead = this.threadPoolConfig != null ? ThreadPoolBulkhead.of(this.serviceName, this.threadPoolConfig) : this.registry.threadPoolBulkhead(this.serviceName);
                decorator = decorator.withThreadPoolBulkhead(threadPoolBulkhead);
            } else if (this.useSemaphore) {
                Bulkhead bulkhead = this.bulkheadConfig != null ? Bulkhead.of(this.serviceName, this.bulkheadConfig) : this.registry.bulkhead(this.serviceName);
                decorator = decorator.withBulkhead(bulkhead);
            }

            if (this.useTimeLimiter) {
                TimeLimiter timeLimiter = this.timeLimiterConfig != null ? TimeLimiter.of(this.timeLimiterConfig) : this.registry.timeLimiter(this.serviceName);
                decorator = decorator.withTimeLimiter(timeLimiter);
            }

            if (useRetry) {
                Retry retry = retryConfig != null ? Retry.of(serviceName, retryConfig) : registry.retry(serviceName);
                decorator = decorator.withRetry(retry);
            }

            if (useRateLimiter) {
                RateLimiter rateLimiter = rateLimiterConfig != null ? RateLimiter.of(serviceName, rateLimiterConfig) : registry.rateLimiter(serviceName);
                decorator = decorator.withRateLimiter(rateLimiter);
            }

            if (this.fallback != null) {
                decorator = decorator.withFallback(this.fallback);
            }

            return decorator.execute();
        }

        public T execute(Supplier<T> supplier) {
            this.supplier = supplier;
            return execute();
        }
    }
}
