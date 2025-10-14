package com.example.demo.controller;

import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
public class RedisController {

    private final UserService userService;

    @PostMapping("/redis-cluster/save")
    public String redisClusterSave() {
        userService.saveUserField("user", "id1", "Alice");
        userService.saveUserField("user", "id2", "Bob");

        return "success";
    }

    @GetMapping("/redis-cluster/get")
    public Object redisClusterGet(@RequestParam String userId) {
        return userService.getData("user", userId);
    }
}
