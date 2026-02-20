package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.RateLimitPolicy;
import com.sagnikverse.rate_limiter.entity.RequestLog;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.entity.TokenBucket;
import com.sagnikverse.rate_limiter.exception.RateLimitExceededException;
import com.sagnikverse.rate_limiter.repository.RateLimitPolicyRepository;
import com.sagnikverse.rate_limiter.repository.RequestLogRepository;
import com.sagnikverse.rate_limiter.repository.TokenBucketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenBucketService {

    private final TokenBucketRepository bucketRepository;
    private final RateLimitPolicyRepository policyRepository;
    private final SubscriptionService subscriptionService;
    private final RequestLogRepository requestLogRepository;
    
    private void logRequest(String identifier, Tier tier, boolean allowed) {

        requestLogRepository.save(
                RequestLog.builder()
                        .identifier(identifier)
                        .tier(tier)
                        .allowed(allowed)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @Transactional
    public boolean allowRequest(String identifier) {

        Tier tier = subscriptionService.getTier(identifier);

        // UNLIMITED bypass
        if (tier == Tier.UNLIMITED) {
            logRequest(identifier, tier, true);
            return true;
        }

        RateLimitPolicy policy = policyRepository
                .findByTierAndActiveTrue(tier)
                .orElseThrow(() ->
                        new RuntimeException("Policy not found"));

        TokenBucket bucket = bucketRepository
                .findByIdentifierForUpdate(identifier)
                .orElse(null);

        // If bucket exists but tier policy changed (upgrade/downgrade)
        if (bucket != null &&
                (!bucket.getCapacity().equals(policy.getCapacity()) ||
                        !bucket.getRefillRate().equals(policy.getRefillRate()))) {

            // Delete old bucket and recreate fresh
            bucketRepository.delete(bucket);
            bucket = null;
        }

        if (bucket == null) {
            bucket = createBucket(identifier, policy);
        }

        refillTokens(bucket);

        boolean allowed = false;

        if (bucket.getTokens() >= 1) {
            bucket.setTokens(bucket.getTokens() - 1);
            allowed = true;
        }

        logRequest(identifier, tier, allowed);

        return allowed;
    }




    private TokenBucket createBucket(String identifier,
                                     RateLimitPolicy policy) {

        TokenBucket bucket = new TokenBucket();
        bucket.setIdentifier(identifier);
        bucket.setCapacity(policy.getCapacity());
        bucket.setRefillRate(policy.getRefillRate());
        bucket.setTokens((double) policy.getCapacity());
        bucket.setLastRefill(LocalDateTime.now());

        return bucketRepository.save(bucket);
    }

    private void refillTokens(TokenBucket bucket) {

        LocalDateTime now = LocalDateTime.now();

        long seconds = Duration
                .between(bucket.getLastRefill(), now)
                .getSeconds();

        double newTokens = seconds * bucket.getRefillRate();

        bucket.setTokens(
                Math.min(bucket.getCapacity(),
                        bucket.getTokens() + newTokens)
        );

        bucket.setLastRefill(now);
    }
}
