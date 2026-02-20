package com.sagnikverse.rate_limiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/data")
    public String test() {
        return "Request Successful";
    }

    @GetMapping("/burst")
    public String burstTest() {
        return "OK";
    }
}
