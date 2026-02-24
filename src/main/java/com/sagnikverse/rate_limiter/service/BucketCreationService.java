package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.*;
import com.sagnikverse.rate_limiter.repository.TokenBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BucketCreationService {

    private final TokenBucketRepository bucketRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TokenBucket createBucket(String identifier,
                                    RateLimitPolicy policy) {

        TokenBucket bucket = new TokenBucket();
        bucket.setIdentifier(identifier);
        bucket.setCapacity(policy.getCapacity());
        bucket.setRefillRate(policy.getRefillRate());
        bucket.setTokens(policy.getCapacity().doubleValue());
        bucket.setLastRefill(LocalDateTime.now());

        return bucketRepository.save(bucket);
    }
}