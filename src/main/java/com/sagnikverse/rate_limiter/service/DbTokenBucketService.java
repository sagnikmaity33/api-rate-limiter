package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.*;
import com.sagnikverse.rate_limiter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
            bucket.setCapacity(policy.getCapacity());
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
}