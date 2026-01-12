package com.example.demo.resilience.context;

import io.github.resilience4j.core.ContextPropagator;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ThreadContextPropagator<T> implements ContextPropagator<T> {
    private final ContextOperations<T> contextOperations;

    public ThreadContextPropagator(ContextOperations<T> contextOperations) {
        this.contextOperations = contextOperations;
    }

    public Supplier<Optional<T>> retrieve() {
        return () -> {
            return Optional.ofNullable(this.contextOperations.getCopyOfContextMap());
        };
    }

    public Consumer<Optional<T>> copy() {
        return (contextOptional) -> {
            if (contextOptional != null && contextOptional.isPresent()) {
                T context = contextOptional.get();
                this.contextOperations.setContextMap(context);
            } else {
                this.contextOperations.clear();
            }

        };
    }

    public Consumer<Optional<T>> clear() {
        return (contextOptional) -> {
            this.contextOperations.clear();
        };
    }

    public interface ContextOperations<T> {
        T getCopyOfContextMap();

        void setContextMap(T var1);

        void clear();
    }
}
