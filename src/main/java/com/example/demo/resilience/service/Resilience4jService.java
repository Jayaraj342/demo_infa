package com.example.demo.resilience.service;

import com.example.demo.exception.RetryableRuntimeException;
import org.springframework.stereotype.Service;

@Service
public class Resilience4jService {

    public String run() throws Exception {
        Thread.sleep(3000);
//        if (Math.random() > 0.5) {
//            throw new RetryableRuntimeException("my exception");
//        }
        return "resilience4j";
    }
}
