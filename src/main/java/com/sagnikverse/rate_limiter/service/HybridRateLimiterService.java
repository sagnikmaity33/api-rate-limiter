package com.sagnikverse.rate_limiter.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HybridRateLimiterService implements RateLimiterService {

    private final RedisTokenBucketService redisService;
    private final DbTokenBucketService dbService;

    @Override
    @CircuitBreaker(name = "redisRateLimiter", fallbackMethod = "fallbackToDb")
    public boolean allowRequest(String identifier) {
        return redisService.allowRequest(identifier);
    }

    // MUST match signature: (String, Throwable)
    public boolean fallbackToDb(String identifier, Throwable ex) {
        System.out.println("Redis unavailable. Falling back to DB.");
        return dbService.allowRequest(identifier);
    }
}