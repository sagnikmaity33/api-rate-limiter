package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.*;
import com.sagnikverse.rate_limiter.repository.RateLimitPolicyRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor

public class RedisTokenBucketService  {

    private final StringRedisTemplate redisTemplate;
    public boolean allowRequest(String identifier)

    // -----------------------------
    // Lua Script with TTL
    // -----------------------------
    private static final String LUA_SCRIPT =
            "local tokens = redis.call(\"HGET\", KEYS[1], \"tokens\")\n" +
                    "local last_refill = redis.call(\"HGET\", KEYS[1], \"last_refill\")\n" +
                    "\n" +
                    "local capacity = tonumber(ARGV[1])\n" +
                    "local refill_rate = tonumber(ARGV[2])\n" +
                    "local now = tonumber(ARGV[3])\n" +
                    "local ttl = tonumber(ARGV[4])\n" +
                    "\n" +
                    "if not tokens then\n" +
                    "    tokens = capacity\n" +
                    "    last_refill = now\n" +
                    "else\n" +
                    "    tokens = tonumber(tokens)\n" +
                    "    last_refill = tonumber(last_refill)\n" +
                    "end\n" +
                    "\n" +
                    "local delta = math.max(0, now - last_refill)\n" +
                    "local refill = delta * refill_rate\n" +
                    "tokens = math.min(capacity, tokens + refill)\n" +
                    "\n" +
                    "local allowed = 0\n" +
                    "\n" +
                    "if tokens >= 1 then\n" +
                    "    tokens = tokens - 1\n" +
                    "    allowed = 1\n" +
                    "end\n" +
                    "\n" +
                    "redis.call(\"HSET\", KEYS[1], \"tokens\", tokens)\n" +
                    "redis.call(\"HSET\", KEYS[1], \"last_refill\", now)\n" +
                    "redis.call(\"EXPIRE\", KEYS[1], ttl)\n" +
                    "\n" +
                    "return allowed";


    private final DefaultRedisScript<Long> redisScript =
            new DefaultRedisScript<>(LUA_SCRIPT, Long.class);



    @CircuitBreaker(name = "redisRateLimiter", fallbackMethod = "fallbackToDb")
    public boolean consume(String bucketKey,
                           Integer capacity,
                           Integer refillRate) {

        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(bucketKey),
                capacity.toString(),
                refillRate.toString(),
                String.valueOf(Instant.now().getEpochSecond()),
                String.valueOf(3600)
        );

        return result != null && result == 1;
    }

    public boolean fallbackToDb(String bucketKey,
                                Integer capacity,
                                Integer refillRate,
                                Throwable ex) {

        System.out.println("Redis unavailable. Falling back to DB.");
        return dbService.consume(bucketKey, capacity, refillRate);
    }
}