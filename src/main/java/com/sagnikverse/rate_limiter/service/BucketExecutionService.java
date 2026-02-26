package com.sagnikverse.rate_limiter.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BucketExecutionService {


    private final RedisTokenBucketService redisService;


    public boolean execute(String bucketKey, Integer capacity, Integer refillRate) {
        return redisService.consume(bucketKey, capacity, refillRate);
    }
}
