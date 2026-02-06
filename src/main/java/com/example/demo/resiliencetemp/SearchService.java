package com.example.demo.resiliencetemp;

import com.example.demo.resiliencetemp.core.ResilientServiceClient;
import com.example.demo.resiliencetemp.exception.ExceptionUtil;
import com.example.demo.resiliencetemp.exception.ResilienceException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.concurrent.TimeoutException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class SearchService {

    public static final String CCGFSEARCH = "ccgfsearch";

    private final ResilientServiceClient resilientServiceClient = ResilientServiceClient.builder(CCGFSEARCH).withTimeLimiter().withRetry().build();

    public String test() {
        try {
            return resilientServiceClient.execute(
                    () -> {
//                    if (true) {
//                        throw new ResilienceException("", new HttpServerErrorException(HttpStatusCode.valueOf(SERVICE_UNAVAILABLE.value())));
//                    }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw ExceptionUtil.throwOriginal(e);
                        }

//                        if (true) {
//                            throw new HttpServerErrorException(HttpStatusCode.valueOf(503));
//                        }

                        return "Hello";
                    }
            );
        } catch (Exception ex) {
            throw new ResilienceException("", ex);
        }

    }
}
