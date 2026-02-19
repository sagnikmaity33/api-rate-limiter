package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.RateLimitPolicy;
import com.sagnikverse.rate_limiter.entity.TokenBucket;
import com.sagnikverse.rate_limiter.exception.RateLimitExceededException;
import com.sagnikverse.rate_limiter.repository.RateLimitPolicyRepository;
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

    @Transactional
    public void allowRequest(String identifier) {

        String type = identifier.split(":")[0];

        RateLimitPolicy policy = policyRepository
                .findByIdentifierTypeAndActiveTrue(type)
                .orElseThrow(() ->
                        new RuntimeException("Policy not found"));

        TokenBucket bucket = bucketRepository
                .findByIdentifier(identifier)
                .orElseGet(() -> createBucket(identifier, policy));

        refillTokens(bucket);

        if (bucket.getTokens() >= 1) {
            bucket.setTokens(bucket.getTokens() - 1);
            bucketRepository.save(bucket);
            return;
        }

        long retryAfter = 1;

        throw new RateLimitExceededException(
                retryAfter,
                bucket.getCapacity(),
                bucket.getTokens()
        );
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
