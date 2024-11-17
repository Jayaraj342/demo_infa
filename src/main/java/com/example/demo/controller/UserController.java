package com.example.demo.controller;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/add-user")
    public void addUser(@RequestBody User user) {
        userService.addUser(user);
    }

    @GetMapping("/get-user/{id}")
    @Transactional
    public User getUser(@PathVariable int id) {
        return userService.getUser(id);
    }

    @GetMapping("/get-posts/{userId}")
    public List<Post> getPostsForUser(@PathVariable int userId) {
        return userService.getPostsForUser(userId);
    }
}
