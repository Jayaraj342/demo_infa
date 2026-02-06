package com.example.demo.resiliencetemp;

import com.example.demo.resiliencetemp.exception.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TempController {

    @Autowired
    private TempProxy tempProxy;

    @GetMapping("/temp")
    public String temp(@RequestParam(required = false) Integer id) {
        try {
            return tempProxy.callMethod();
        } catch (Exception ex) {
            throw ExceptionUtil.throwOriginal(ex);
        }
    }
}
