package com.example.demo.controller;

import com.example.demo.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String eatTime() {
        String res;
        Date start = new Date();
        try {
            productService.eatTime();
            res = "success! after " + getSeconds(start, new Date()) + " seconds";
        } catch (Exception e) {
            res = "timed out! after " + getSeconds(start, new Date()) + " seconds";
        }

        return res;
    }

    private static long getSeconds(Date start, Date end) {
        return (end.getTime() - start.getTime()) / 1000;
    }
}
