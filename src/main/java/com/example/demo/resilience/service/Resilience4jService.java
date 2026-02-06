package com.example.demo.resilience.service;

import com.example.demo.exception.ErrorData;
import com.example.demo.exception.ErrorInfo;
import com.example.demo.exception.RetryableRuntimeException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class Resilience4jService {

    public String run() throws Exception {
//        Thread.sleep(6000);
//        if (Math.random() > 0.5) {
//            ErrorInfo errorInfo = new ErrorInfo();
//            ErrorData error = new ErrorData();
//            error.setCode("1234");
//            errorInfo.setError(error);
//            throw new RetryableRuntimeException(errorInfo);
//        }
        if (true) {
            throw new ResourceAccessException("resource not found!");
        }
        return "resilience4j";
    }

    public String http(int code) throws Exception {
        if (true) {
            throw new ResourceAccessException("Not found");
        }
        // 1. Configure timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);

        RestTemplate restTemplate = new RestTemplate(factory);
        String url = "http://Mock.httpstatus.io/" + code;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        headers.set("Accept", "*/*");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 2. Execute with exchange to get metadata (status codes, headers)
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
