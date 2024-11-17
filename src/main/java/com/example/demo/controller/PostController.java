package com.example.demo.controller;

import com.example.demo.model.Post;
import com.example.demo.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/add-post")
    public void addPost(@RequestBody Post post) {
        postService.addPost(post);
    }

    @GetMapping("/get-post/{id}")
    public Post getPost(@PathVariable int id) {
        return postService.getPost(id);
    }
}
