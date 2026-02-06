package com.example.demo.resiliencetemp.context;

import io.github.resilience4j.core.ContextPropagator;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generic thread context propagator for Resilience4j.
 * Abstracts context map operations to support different context implementations.
 *
 * @param <T> The type of context map to propagate
 */
public class ThreadContextPropagator<T> implements ContextPropagator<T> {

    private final ContextOperations<T> contextOperations;

    /**
     * Interface defining operations for context map management
     *
     * @param <T> The type of context map
     */
    public interface ContextOperations<T> {
        /**
         * Get a copy of the current context map
         * @return Copy of context map or null if none exists
         */
        T getCopyOfContextMap();

        /**
         * Set the context map
         * @param context The context map to set
         */
        void setContextMap(T context);

        /**
         * Clear the context
         */
        void clear();
    }

    /**
     * Construct a ThreadContextPropagator with specific context operations
     *
     * @param contextOperations The context operations implementation
     */
    public ThreadContextPropagator(ContextOperations<T> contextOperations) {
        this.contextOperations = contextOperations;
    }

    @Override
    public Supplier<Optional<T>> retrieve() {
        return () -> Optional.ofNullable(contextOperations.getCopyOfContextMap());
    }

    @Override
    public Consumer<Optional<T>> copy() {
        return contextOptional -> {
            if (contextOptional != null && contextOptional.isPresent()) {
                T context = contextOptional.get();
                contextOperations.setContextMap(context);
            } else {
                contextOperations.clear();
            }
        };
    }

    @Override
    public Consumer<Optional<T>> clear() {
        return contextOptional -> contextOperations.clear();
    }
}

