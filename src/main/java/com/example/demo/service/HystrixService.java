package com.example.demo.service;

import com.example.demo.exception.RetryableRuntimeException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
public class HystrixService {

    @HystrixCommand(commandKey = "service")
    public String test() throws Exception {
        Thread.sleep(5000);
        return "Hystrix";
    }

//    public String fallbackTest(Throwable t) throws Exception {
//        if (t instanceof ResourceAccessException) {
//            throw (ResourceAccessException) t;
//        } else if (t instanceof HystrixTimeoutException) {
//            throw (HystrixTimeoutException) t;
//        } else if (t instanceof RetryableRuntimeException) {
//            throw (RetryableRuntimeException) t;
//        } else {
//            throw (Exception) t;
//        }
//    }
}
