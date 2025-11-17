package com.example.demo.controller;

import com.example.demo.exception.RetryableRuntimeException;
import com.example.demo.service.HystrixService;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@RestController
@Slf4j
@AllArgsConstructor
public class HystrixTestController {

    private final HystrixService hystrixService;

    @GetMapping("/hystrix")
    public String hystrix() {
        return "hystrix1";
    }

    @GetMapping("/hystrix/test")
    public String hystrixTest() {

        String test = "init";
        try {
            test = hystrixService.test();
            log.info("worked1 ---------------------------");
        } catch (Exception e) {
            log.error("Exception : ", e);
        }

        return test;
    }

    @GetMapping("/hystrix/resttemplate/test")
    public String hystrixResttemplateTest() {

        String test = "init2";
        try {
            RetryTemplate retryTemplate = getRetryTemplate();
            test = retryTemplate.execute(context -> {
                log.info("============================Trying to call hystrix test, attempt " + (context.getRetryCount() + 1));
                return hystrixService.test();
            });

            log.info("worked2 ---------------------------");
        } catch (Exception e) {
            log.error("Exception : ", e);
        }

        return test;
    }

    private RetryTemplate getRetryTemplate() {
        RetryTemplate retryDefaultTemplate = new RetryTemplate();
        retryDefaultTemplate.setRetryPolicy(new DualDelegateRetryPolicy(3));

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000);
        retryDefaultTemplate.setBackOffPolicy(fixedBackOffPolicy);

        return retryDefaultTemplate;
    }

    static class SaasCustomRetryPolicy implements RetryPolicy {

        private final SimpleRetryPolicy delegate;

        public SaasCustomRetryPolicy(int maxAttempts) {
            Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
            retryableExceptions.put(ResourceAccessException.class, true);
            retryableExceptions.put(HystrixTimeoutException.class, true);
            retryableExceptions.put(TimeoutException.class, true);
            retryableExceptions.put(RetryableRuntimeException.class, true);
            retryableExceptions.put(HystrixRuntimeException.class, true);
            // HystrixRuntimeException is retried only if underlying cause is TimeoutException or ResourceAccessException as its first cause

            this.delegate = new SimpleRetryPolicy(maxAttempts, retryableExceptions);
        }

        @Override
        public boolean canRetry(RetryContext context) {
            Throwable throwable = context.getLastThrowable();
            if (throwable == null) {
                return delegate.canRetry(context);
            }

            if (throwable instanceof HystrixRuntimeException) {
                if (hasAllowedCause(throwable)) {
                    return delegate.canRetry(context);
                } else {
                    return false; // no retry if cause not TimeoutException or ResourceAccessException
                }
            }

            return delegate.canRetry(context);
        }

        private boolean hasAllowedCause(Throwable throwable) {
            Throwable cause = throwable.getCause();// check cause of first level only (not checking recursively)
            return cause instanceof TimeoutException || cause instanceof ResourceAccessException;
        }

        @Override
        public RetryContext open(RetryContext context) {
            return delegate.open(context);
        }

        @Override
        public void close(RetryContext context) {
            delegate.close(context);
        }

        @Override
        public void registerThrowable(RetryContext context, Throwable throwable) {
            delegate.registerThrowable(context, throwable);
        }
    }

    public static class DualDelegateRetryPolicy implements RetryPolicy {

        private final SimpleRetryPolicy defaultDelegate;
        private final SimpleRetryPolicy causeAwareDelegate;

        public DualDelegateRetryPolicy(int maxAttempts) {
            Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
            retryableExceptions.put(ResourceAccessException.class, true);
            retryableExceptions.put(HystrixTimeoutException.class, true);
            retryableExceptions.put(TimeoutException.class, true);
            retryableExceptions.put(RetryableRuntimeException.class, true);

            // Delegate without cause traversal
            this.defaultDelegate = new SimpleRetryPolicy(maxAttempts, retryableExceptions, false); // traverseCauses = false

            Map<Class<? extends Throwable>, Boolean> retryableExceptionsCheckCause = new HashMap<>();
            retryableExceptionsCheckCause.put(ResourceAccessException.class, true);
            retryableExceptionsCheckCause.put(TimeoutException.class, true);

            // Delegate with cause traversal for HystrixRuntimeException only
            this.causeAwareDelegate = new SimpleRetryPolicy(maxAttempts, retryableExceptionsCheckCause, true); // traverseCauses = true
        }

        @Override
        public boolean canRetry(RetryContext context) {
            Throwable throwable = context.getLastThrowable();
            if (throwable instanceof HystrixRuntimeException) {
                return causeAwareDelegate.canRetry(context);
            } else {
                // for other exceptions use default delegate without cause check
                return defaultDelegate.canRetry(context);
            }
        }

        @Override
        public RetryContext open(RetryContext parent) {
            // Use defaultDelegate's open (or causeAwareDelegate's, both behave same)
            return defaultDelegate.open(parent);
        }

        @Override
        public void close(RetryContext context) {
            defaultDelegate.close(context);
            causeAwareDelegate.close(context);
        }

        @Override
        public void registerThrowable(RetryContext context, Throwable throwable) {
            defaultDelegate.registerThrowable(context, throwable);
            causeAwareDelegate.registerThrowable(context, throwable);
        }
    }
}
