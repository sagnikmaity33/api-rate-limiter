package com.sagnikverse.rate_limiter.controller;

import com.sagnikverse.rate_limiter.repository.RequestLogRepository;
import com.sagnikverse.rate_limiter.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final RequestLogRepository repository;

    @GetMapping("/overview")
    public Object overview() {
        return analyticsService.overviewToday();
    }

    @GetMapping("/top-violators")
    public Object topViolators() {
        return repository.topViolators(PageRequest.of(0, 10));
    }

    @GetMapping("/endpoints")
    public Object endpointStats() {
        return repository.mostRequestedEndpoints();
    }

    @GetMapping("/blocked-endpoints")
    public Object blockedEndpoints() {
        return repository.mostBlockedEndpoints();
    }

    @GetMapping("/timeseries")
    public Object timeseries() {
        return repository.requestsPerMinute();
    }
}