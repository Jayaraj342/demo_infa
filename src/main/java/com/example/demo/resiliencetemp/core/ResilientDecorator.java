package com.example.demo.resiliencetemp.core;

import com.example.demo.resiliencetemp.exception.ResilienceException;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.ContextPropagator;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.example.demo.resiliencetemp.exception.ExceptionUtil.throwOriginal;

/**
 * Fluent decorator builder for applying Resilience4j patterns.
 * Provides a convenient API for decorating suppliers with multiple resilience patterns
 * following the recommended order: CircuitBreaker -> RateLimiter -> Bulkhead -> TimeLimiter -> Retry -> Fallback
 */
public class ResilientDecorator {

    private ResilientDecorator() {
    }

    /**
     * Start decorating a supplier with resilience patterns
     */
    public static <T> SupplierDecorator<T> of(Supplier<T> supplier) {
        return new SupplierDecorator<>(supplier);
    }

    /**
     * Builder for decorating suppliers with resilience patterns
     */
    public static class SupplierDecorator<T> {
        private Supplier<T> supplier;

        public SupplierDecorator(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public SupplierDecorator<T> withCircuitBreaker(CircuitBreaker circuitBreaker) {
            this.supplier = Decorators.ofSupplier(supplier)
                    .withCircuitBreaker(circuitBreaker)
                    .decorate();
            return this;
        }

        public SupplierDecorator<T> withRateLimiter(RateLimiter rateLimiter) {
            this.supplier = Decorators.ofSupplier(supplier)
                    .withRateLimiter(rateLimiter)
                    .decorate();
            return this;
        }

        public SupplierDecorator<T> withBulkhead(Bulkhead bulkhead) {
            this.supplier = Decorators.ofSupplier(supplier)
                    .withBulkhead(bulkhead)
                    .decorate();
            return this;
        }

        public SupplierDecorator<T> withThreadPoolBulkhead(ThreadPoolBulkhead threadPoolBulkhead) {
            // ThreadPoolBulkhead returns CompletionStage, so we need to block and get the result
            Supplier<T> currentSupplier = this.supplier;
            this.supplier = () -> {
                return threadPoolBulkhead.executeSupplier(currentSupplier)
                        .toCompletableFuture()
                        .join();  // Block and wait for result
            };
            return this;
        }

        public SupplierDecorator<T> withTimeLimiter(TimeLimiter timeLimiter) {
            Supplier<T> currentSupplier = this.supplier;
            this.supplier = () -> {
                try {
                    return timeLimiter.executeFutureSupplier(() -> CompletableFuture.supplyAsync(ContextPropagator.decorateSupplier(ResiliencePolicyRegistry.INSTANCE.getContextPropagators(), currentSupplier)));
                } catch (TimeoutException e) {
                    throw new ResilienceException(e);
                } catch (Exception e) {
                    throw throwOriginal(e);
                }

            };
            return this;
        }


        public SupplierDecorator<T> withRetry(Retry retry) {
            this.supplier = Decorators.ofSupplier(supplier)
                    .withRetry(retry)
                    .decorate();
            return this;
        }

        public SupplierDecorator<T> withFallback(Function<Throwable, T> fallbackSupplier) {
            this.supplier = Decorators.ofSupplier(supplier)
                    .withFallback(fallbackSupplier)
                    .decorate();
            return this;
        }

        /**
         * Complete the decoration and get the final supplier
         */
        public Supplier<T> decorate() {
            return supplier;
        }

        /**
         * Execute the decorated supplier immediately
         */
        public T execute() {
            return supplier.get();
        }
    }
}
