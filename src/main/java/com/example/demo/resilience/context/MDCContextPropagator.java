package com.example.demo.resilience.context;

import org.slf4j.MDC;

import java.util.Map;

/**
 * MDC-specific implementation of ThreadContextPropagator for SLF4J MDC.
 */
public class MDCContextPropagator extends ThreadContextPropagator<Map<String, String>> {

    public MDCContextPropagator() {
        super(new MdcContextOperations());
    }

    /**
     * MDC-specific context operations implementation
     */
    private static class MdcContextOperations implements ContextOperations<Map<String, String>> {

        @Override
        public Map<String, String> getCopyOfContextMap() {
            return MDC.getCopyOfContextMap();
        }

        @Override
        public void setContextMap(Map<String, String> context) {
            if (context != null && !context.isEmpty()) {
                MDC.setContextMap(context);
            }
        }

        @Override
        public void clear() {
            MDC.clear();
        }
    }
}