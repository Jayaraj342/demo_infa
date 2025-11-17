//package com.example.demo.service;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//public class UserService {
//
//    // K = Redis Key (e.g., "user:101"), HK = Hash Key (e.g., "name"), V = Hash Value (e.g., "Alice")
//    // user:101 will be like 1 HashMap
//    private HashOperations<String, String, Object> hashOperations;
//
//    // Inject the configured RedisTemplate
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    public UserService(RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    // Initialize HashOperations after the RedisTemplate is ready
//    @PostConstruct
//    private void init() {
//        this.hashOperations = redisTemplate.opsForHash();
//    }
//
//    /**
//     * Corresponds to the HSET command: HSET KEY FIELD VALUE
//     */
//    public void saveUserField(String redisKey, String fieldName, Object fieldValue) {
//
//        // HSET user id1 "Alice"
//        hashOperations.put(redisKey, fieldName, fieldValue);
//        System.out.println("HSET: " + fieldName + " stored for " + redisKey);
//    }
//
//    public Object getData(String redisKey, String userId) {
//        return hashOperations.get(redisKey, userId);
//    }
//}
