package com.example.demo.resiliencetemp;

import com.example.demo.resiliencetemp.exception.ExceptionUtil;
import com.example.demo.resiliencetemp.exception.ResilienceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.TimeoutException;

@Component
public class TempProxy {

    @Autowired
    private SearchService searchService;

    @Retryable(
            retryFor = {ResilienceException.class, TimeoutException.class, ResourceAccessException.class},
            maxAttemptsExpression = "${saas.retry.maxAttempts:5}",
            backoff = @Backoff(delayExpression = "${saas.retry.backoff:1000}", maxDelayExpression = "${saas.retry.maxDelay:2000}")
    )
    public String callMethod() {
        try {
            return searchService.test();
        } catch (ResilienceException ex) {
            throw ExceptionUtil.throwOriginal(ex);
        }
    }
}
