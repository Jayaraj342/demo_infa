package com.example.demo.resiliencetemp.context;

import io.github.resilience4j.core.ContextPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MDCContextPropagator implements ContextPropagator<Map<String, String>> {
    private static final Logger logger = LoggerFactory.getLogger(MDCContextPropagator.class);

    @Override
    public Supplier<Optional<Map<String, String>>> retrieve() {
        return () -> {
            logger.trace("getting ContextMap : {}", MDC.getCopyOfContextMap());
            return Optional.ofNullable(MDC.getCopyOfContextMap());
        };
    }

    @Override
    public Consumer<Optional<Map<String, String>>> copy() {
        return (copyOfContextMap) -> {
            logger.trace("setting copyOfContextMap : {}", copyOfContextMap);
            copyOfContextMap.ifPresent(MDC::setContextMap);
        };
    }

    @Override
    public Consumer<Optional<Map<String, String>>> clear() {
        return (contextMap) -> MDC.clear();
    }
}