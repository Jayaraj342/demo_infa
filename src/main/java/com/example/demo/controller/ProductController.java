package com.example.demo.controller;

import com.example.demo.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@Slf4j
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String eatTime() {
        log.info("/test API called ------------------------------------------------------------------------------");
        String res;
        Date start = new Date();
        try {
            productService.eatTime();
            res = "success! after " + getSeconds(start, new Date()) + " seconds started : " + start + " ended : " + new Date();
        } catch (Exception e) {
            res = "timed out! after " + getSeconds(start, new Date()) + " seconds started : " + start + " ended : " + new Date();
        }

        return res;
    }

    private static long getSeconds(Date start, Date end) {
        return (end.getTime() - start.getTime()) / 1000;
    }
}
