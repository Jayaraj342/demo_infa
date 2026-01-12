package com.example.demo.resilience;

import com.example.demo.exception.RetryableRuntimeException;
import com.example.demo.resilience.context.MDCContextPropagator;
import com.example.demo.resilience.service.Resilience4jService;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.core.ContextPropagator;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@RestController
@Slf4j
public class Resilience4jController {

    private final Resilience4jService resilience4jService;
    private final Bulkhead semaphoreBulkhead;
    private final ThreadPoolBulkhead threadPoolBulkhead;

    public Resilience4jController(Resilience4jService resilience4jService) {
        this.resilience4jService = resilience4jService;

        // Create a custom configuration for a Bulkhead
        BulkheadConfig semaphoreBulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(0)
                .build();

        // Given
        semaphoreBulkhead = Bulkhead.of("myBulkhead", semaphoreBulkheadConfig);

        // Create a custom configuration for a Bulkhead
        ThreadPoolBulkheadConfig threadPoolBulkheadConfig = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(1)
                .coreThreadPoolSize(1)
                .queueCapacity(0)
                .build();

        // Given
        threadPoolBulkhead = ThreadPoolBulkhead.of("myBulkhead", threadPoolBulkheadConfig);
    }

    @GetMapping("/resilience4j")
    public String resilience4j() {

        // set MDC dummy context
        MDC.setContextMap(Map.of("my-key", "my-value"));

        // When I decorate my function
        Supplier<String> supplier = () -> {
            try {
                return resilience4jService.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };


        String response;
        try {
            response = myFunction(supplier);
        } catch (Exception ex) {
            if (ex instanceof RetryableRuntimeException) {
                log.info("---------------------------- RetryableRuntimeException");
            } else {
                log.info("---------------------------- didn't get RetryableRuntimeException");
            }
            throw ex;
        }

        return response;
    }

    public String myFunction(Supplier<String> supplier) {
        return withThreadPoolBulkheadIsolation(withTimeLimiter(supplier)).get();
    }

    public <T> Supplier<T> withSemaphoreIsolation(Supplier<T> supplier) {
        return Decorators.ofSupplier(supplier)
                .withBulkhead(semaphoreBulkhead)
                .decorate();
    }

    public <T> Supplier<T> withThreadPoolBulkheadIsolation(Supplier<T> supplier) {
        return () -> {
            try {
                return threadPoolBulkhead.executeSupplier(supplier)
                        .toCompletableFuture()
                        .join();
            } catch (Exception e) {
                throw new RuntimeException("TimeLimiter execution failed", e);
            }
        };
    }

    public <T> Supplier<T> withTimeLimiter(Supplier<T> supplier) {
        // Create a TimeLimiter configuration
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))  // set your timeout duration
                .cancelRunningFuture(true)
                .build();

        TimeLimiter timeLimiter = TimeLimiter.of("myTimeLimiter", timeLimiterConfig);

        return () -> {
            try {
                return timeLimiter.executeFutureSupplier(
                        () -> CompletableFuture.supplyAsync(
                                ContextPropagator.decorateSupplier(List.of(new MDCContextPropagator()), supplier)
                        )
                );
            } catch (Exception e) {
                throw new RuntimeException("TimeLimiter execution failed", e);
            }
        };
    }
}
