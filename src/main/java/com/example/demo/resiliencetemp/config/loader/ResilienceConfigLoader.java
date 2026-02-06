package com.example.demo.resiliencetemp.config.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads resilience configuration from YML files.
 * Supports configuration for: CircuitBreaker, Bulkhead, TimeLimiter, Retry, and RateLimiter patterns.
 */
public class ResilienceConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ResilienceConfigLoader.class);

    private static final String DEFAULT_CONFIG_FILE = "resilience.yaml";
    private static final String RESILIENCE4J_CONFIG_KEY = "resilience4j";
    private static final String INSTANCES_KEY = "instances";

    // Pattern configuration keys
    private static final String PATTERN_CIRCUIT_BREAKER = "circuitbreaker";
    private static final String PATTERN_BULKHEAD = "bulkhead";
    private static final String PATTERN_THREAD_POOL_BULKHEAD = "threadpool_bulkhead";
    private static final String PATTERN_TIME_LIMITER = "timelimiter";
    private static final String PATTERN_RETRY = "retry";
    private static final String PATTERN_RATE_LIMITER = "ratelimiter";

    private Map<String, Object> config = new HashMap<>();

    public ResilienceConfigLoader() {
        this(DEFAULT_CONFIG_FILE);
    }

    public ResilienceConfigLoader(String configFile) {
        loadConfig(configFile);
    }

    private void loadConfig(String configFile) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (inputStream == null) {
                LOG.warn("Configuration file {} not found, using empty defaults", configFile);
                return;
            }

            Yaml yaml = new Yaml();
            Map<String, Object> loadedConfig = yaml.load(inputStream);

            if (loadedConfig == null) {
                LOG.warn("Configuration file {} is empty, using defaults", configFile);
                config = new HashMap<>();
            } else {
                config = loadedConfig;
                LOG.info("Successfully loaded configuration from {}", configFile);
            }
        } catch (Exception e) {
            LOG.error("Error loading configuration from {}, using empty defaults", configFile, e);
        }
    }

    /**
     * Check if configuration exists for a specific service and resilience pattern.
     *
     * @param serviceName the name of the service
     * @param pattern the resilience pattern (e.g., "circuitbreaker")
     * @return true if configuration exists for the given service and pattern
     */
    @SuppressWarnings("unchecked")
    public boolean hasConfig(String serviceName, String pattern) {
        if (serviceName == null || serviceName.isEmpty() || pattern == null || pattern.isEmpty()) {
            LOG.debug("Invalid parameters: serviceName='{}', pattern='{}'", serviceName, pattern);
            return false;
        }

        try {
            Map<String, Object> resilience4j = getResilience4jConfig();
            if (resilience4j == null) {
                LOG.debug("No resilience4j configuration found");
                return false;
            }

            Map<String, Object> patternConfig = (Map<String, Object>) resilience4j.get(pattern);
            if (patternConfig == null) {
                LOG.debug("No configuration found for pattern: {}", pattern);
                return false;
            }

            Map<String, Object> instances = (Map<String, Object>) patternConfig.get(INSTANCES_KEY);
            if (instances == null) {
                LOG.debug("No instances found for pattern: {}", pattern);
                return false;
            }

            return instances.containsKey(serviceName);
        } catch (Exception e) {
            LOG.debug("Error checking configuration for service {} and pattern {}", serviceName, pattern, e);
            return false;
        }
    }

    /**
     * Get CircuitBreaker configuration for a service.
     *
     * @param serviceName the name of the service
     * @return configuration map, or empty map if not found
     */
    public Map<String, Object> getCircuitBreakerConfig(String serviceName) {
        return getServiceConfig(PATTERN_CIRCUIT_BREAKER, serviceName);
    }

    /**
     * Get Bulkhead configuration for a service.
     *
     * @param serviceName the name of the service
     * @return configuration map, or empty map if not found
     */
    public Map<String, Object> getBulkheadConfig(String serviceName) {
        return getServiceConfig(PATTERN_BULKHEAD, serviceName);
    }

    /**
     * Get ThreadPool Bulkhead configuration for a service.
     *
     * @param serviceName the name of the service
     * @return configuration map, or empty map if not found
     */
    public Map<String, Object> getThreadPoolBulkheadConfig(String serviceName) {
        return getServiceConfig(PATTERN_THREAD_POOL_BULKHEAD, serviceName);
    }

    /**
     * Get TimeLimiter configuration for a service.
     *
     * @param serviceName the name of the service
     * @return configuration map, or empty map if not found
     */
    public Map<String, Object> getTimeLimiterConfig(String serviceName) {
        return getServiceConfig(PATTERN_TIME_LIMITER, serviceName);
    }

    /**
     * Get Retry configuration for a service.
     *
     * @param serviceName the name of the service
     * @return configuration map, or empty map if not found
     */
    public Map<String, Object> getRetryConfig(String serviceName) {
        return getServiceConfig(PATTERN_RETRY, serviceName);
    }

    /**
     * Get RateLimiter configuration for a service.
     *
     * @param serviceName the name of the service
     * @return configuration map, or empty map if not found
     */
    public Map<String, Object> getRateLimiterConfig(String serviceName) {
        return getServiceConfig(PATTERN_RATE_LIMITER, serviceName);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getResilience4jConfig() {
        if (config == null) {
            return null;
        }
        return (Map<String, Object>) config.get(RESILIENCE4J_CONFIG_KEY);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getServiceConfig(String pattern, String serviceName) {
        try {
            Map<String, Object> resilience4j = getResilience4jConfig();
            if (resilience4j == null) {
                return Collections.emptyMap();
            }

            Map<String, Object> patternConfig = (Map<String, Object>) resilience4j.get(pattern);
            if (patternConfig == null) {
                return Collections.emptyMap();
            }

            Map<String, Object> instances = (Map<String, Object>) patternConfig.get(INSTANCES_KEY);
            if (instances == null) {
                return Collections.emptyMap();
            }

            Map<String, Object> serviceConfig = (Map<String, Object>) instances.get(serviceName);
            return serviceConfig != null ? serviceConfig : Collections.emptyMap();
        } catch (ClassCastException e) {
            LOG.error("Configuration structure error for service {} and pattern {}: invalid type", serviceName, pattern, e);
            return Collections.emptyMap();
        } catch (Exception e) {
            LOG.debug("Error getting config for service {} and pattern {}", serviceName, pattern, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Helper method to safely parse Duration from config.
     * Supports formats: "30s", "2s", "100ms", "5m", or ISO-8601 duration strings.
     *
     * @param value the value to parse
     * @param defaultValue the default duration if parsing fails
     * @return parsed Duration or default value
     */
    public static Duration parseDuration(Object value, Duration defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            String strValue = value.toString().trim();

            if (strValue.matches("\\d+s")) {
                return Duration.ofSeconds(Long.parseLong(strValue.replace("s", "")));
            } else if (strValue.matches("\\d+ms")) {
                return Duration.ofMillis(Long.parseLong(strValue.replace("ms", "")));
            } else if (strValue.matches("\\d+m")) {
                return Duration.ofMinutes(Long.parseLong(strValue.replace("m", "")));
            } else {
                return Duration.parse(strValue);
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse duration '{}', using default: {}", value, defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Helper method to safely get Integer from config.
     *
     * @param config the configuration map
     * @param key the key to retrieve
     * @param defaultValue the default value if key not found or parsing fails
     * @return parsed Integer or default value
     */
    public static Integer getInteger(Map<String, Object> config, String key, Integer defaultValue) {
        if (config == null) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            if (value instanceof Integer) {
                return (Integer) value;
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            LOG.warn("Failed to parse integer for key '{}': '{}', using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Helper method to safely get Double from config.
     *
     * @param config the configuration map
     * @param key the key to retrieve
     * @param defaultValue the default value if key not found or parsing fails
     * @return parsed Double or default value
     */
    public static Double getDouble(Map<String, Object> config, String key, Double defaultValue) {
        if (config == null) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            if (value instanceof Double) {
                return (Double) value;
            }
            if (value instanceof Integer) {
                return ((Integer) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            LOG.warn("Failed to parse double for key '{}': '{}', using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Helper method to safely get Boolean from config.
     *
     * @param config the configuration map
     * @param key the key to retrieve
     * @param defaultValue the default value if key not found or parsing fails
     * @return parsed Boolean or default value
     */
    public static Boolean getBoolean(Map<String, Object> config, String key, Boolean defaultValue) {
        if (config == null) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            return Boolean.parseBoolean(value.toString());
        } catch (Exception e) {
            LOG.warn("Failed to parse boolean for key '{}': '{}', using default: {}", key, value, defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Helper method to safely get String from config.
     *
     * @param config the configuration map
     * @param key the key to retrieve
     * @param defaultValue the default value if key not found
     * @return string value or default value
     */
    public static String getString(Map<String, Object> config, String key, String defaultValue) {
        if (config == null) {
            return defaultValue;
        }
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
