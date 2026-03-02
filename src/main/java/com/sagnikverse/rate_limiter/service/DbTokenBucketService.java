package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.*;
import com.sagnikverse.rate_limiter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class DbTokenBucketService {

    private final TokenBucketRepository bucketRepository;
    private final RateLimitPolicyRepository policyRepository;
    private final SubscriptionService subscriptionService;

    @Transactional
    public boolean allowRequest(String identifier) {

        Tier tier = subscriptionService.getTier(identifier);

        RateLimitPolicy policy = policyRepository
                .findByTierAndActiveTrue(tier)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        TokenBucket bucket = bucketRepository
                .findByIdentifierForUpdate(identifier)
                .orElse(null);

        if (bucket == null) {
            bucket = new TokenBucket();
            bucket.setIdentifier(identifier);
            bucket.setCapacity(Double.valueOf(policy.getCapacity()));
            bucket.setRefillRate(policy.getRefillRate());
            bucket.setTokens(policy.getCapacity().doubleValue());
            bucket.setLastRefill(java.time.LocalDateTime.now());

            bucketRepository.save(bucket);
        }

        if (bucket.getTokens() >= 1) {
            bucket.setTokens(bucket.getTokens() - 1);
            return true;
        }

        return false;
    }

    @Transactional
    public boolean consume(String bucketKey,
                           Integer capacity,
                           Double refillRate,
                           Integer cost) {

        TokenBucket bucket = bucketRepository
                .findByIdentifierForUpdate(bucketKey)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        if (bucket == null) {
            bucket = new TokenBucket();
            bucket.setIdentifier(bucketKey);
            bucket.setCapacity(capacity.doubleValue());
            bucket.setRefillRate(refillRate);
            bucket.setTokens(capacity.doubleValue());
            bucket.setLastRefill(now);
            bucketRepository.save(bucket);
        }

        long secondsElapsed = java.time.Duration
                .between(bucket.getLastRefill(), now)
                .getSeconds();

        double refill = secondsElapsed * bucket.getRefillRate();

        double updatedTokens = Math.min(
                bucket.getCapacity(),
                bucket.getTokens() + refill
        );

        bucket.setTokens(updatedTokens);
        bucket.setLastRefill(now);

        if (bucket.getTokens() >= cost) {
            bucket.setTokens(bucket.getTokens() - cost);
            return true;
        }

        return false;
    }
}