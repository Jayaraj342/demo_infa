package com.example.demo.resiliencetemp.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class DefaultRetryPredicate implements Predicate<Throwable> {

    private final List<Class<? extends Throwable>> retryableExceptions = Arrays.asList(
            TimeoutException.class,
            ResourceAccessException.class
    );

    @Override
    public boolean test(Throwable th) {
        if (th == null) {
            return false;
        }

        ResilienceException resilienceEx = ExceptionUtils.throwableOfType(th, ResilienceException.class);
        if (resilienceEx != null) {
            return true;
        }

        HttpServerErrorException serverEx = ExceptionUtils.throwableOfType(th, HttpServerErrorException.class);
        if (serverEx != null) {
            return isRetryable(serverEx.getStatusCode());
        }

        return retryableExceptions
                .stream()
                .anyMatch(type -> ExceptionUtils.throwableOfType(th, type) != null);
    }

    public static boolean isRetryable(HttpStatusCode statusCode) {
        return HttpStatus.SERVICE_UNAVAILABLE.equals(statusCode) || HttpStatus.GATEWAY_TIMEOUT.equals(statusCode);
    }
}
