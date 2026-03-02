package com.sagnikverse.rate_limiter.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestApiController {

    @GetMapping("/users/{id}")
    public String getUser(@PathVariable String id) {
        return "User " + id;
    }

    @GetMapping("/search")
    public String search(@RequestParam String q) {
        return "Search: " + q;
    }

    @PostMapping("/reports/generate")
    public String generateReport() {
        return "Report generated";
    }

    @PostMapping("/ai/analyze")
    public String analyze() {
        return "AI analysis complete";
    }
}