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
    private final RuleEngineService ruleEngineService;

    @Override
    public boolean allowRequest(RequestContext context) {
        return ruleEngineService.evaluate(context);
    }
    // MUST match signature: (String, Throwable)
    public boolean fallbackToDb(String identifier, Throwable ex) {
        System.out.println("Redis unavailable. Falling back to DB.");
        return dbService.allowRequest(identifier);
    }
}