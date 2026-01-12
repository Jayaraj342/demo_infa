package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/mgmtapi/health")
    public void health() {
        System.out.println("/mgmtapi/health called!");
    }
}
