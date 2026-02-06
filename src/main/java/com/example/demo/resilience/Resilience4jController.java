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
import org.springframework.web.bind.annotation.RequestParam;
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
                .maxConcurrentCalls(1)
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
    public String resilience4j(@RequestParam(required = false) Integer id) {
        if (id == null) {
            id = 200;
        }

        // set MDC dummy context
        MDC.setContextMap(Map.of("my-key", "my-value"));

        // When I decorate my function
        Integer finalId = id;
        Supplier<String> supplier = () -> {
            try {
                return resilience4jService.run();
            } catch (Exception e) {
                throw throwOriginal(e);
            }
        };

        String response;
        try {
            response = myFunction(supplier);
        } catch (Exception ex) {
            try {
                // will get completion exception here - unwrap it
                throw throwOriginal(ex);
            } catch (Exception original) {
                if (original instanceof RetryableRuntimeException) {
                    log.info("---------------------------- RetryableRuntimeException");
                } else {
                    log.info("---------------------------- didn't get RetryableRuntimeException");
                }

                throw original;
            }
        }

        return response;
    }

    public String myFunction(Supplier<String> supplier) {
        return withSemaphoreIsolation(withTimeLimiter(supplier)).get();
    }

    public <T> Supplier<T> withSemaphoreIsolation(Supplier<T> supplier) {
        return Decorators.ofSupplier(supplier)
                .withBulkhead(semaphoreBulkhead)
                .decorate();
    }

    public <T> Supplier<T> withThreadPoolBulkheadIsolation(Supplier<T> supplier) {
        return () -> threadPoolBulkhead.executeSupplier(supplier)
                .toCompletableFuture()
                .join();
    }

    public <T> Supplier<T> withTimeLimiter(Supplier<T> supplier) {
        // Create a TimeLimiter configuration
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(50))  // set your timeout duration
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
                throw throwOriginal(e);
            }
        };
    }

    public static RuntimeException throwOriginal(Throwable t) {
        // 1. Unwrap the wrapping layers (CompletionException/ExecutionException)
        if (t instanceof java.util.concurrent.CompletionException || t instanceof java.util.concurrent.ExecutionException) {
            return throwOriginal(t.getCause());
        }

        // 2. "Sneakily" throw the original exception (Checked or Unchecked)
        sneakyThrow(t);
        return new RuntimeException(t); // unreachable
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t; // The compiler erases <T> to Throwable, but treats it as T locally
    }
}
