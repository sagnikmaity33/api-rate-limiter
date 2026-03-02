package com.sagnikverse.rate_limiter.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sagnikverse.rate_limiter.entity.RateLimitPolicy;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.repository.RateLimitPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PolicyCache {

    private final RateLimitPolicyRepository repository;

    private final Cache<Tier, RateLimitPolicy> cache =
            Caffeine.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build();

    public RateLimitPolicy getPolicy(Tier tier) {
        return cache.get(tier,
                t -> repository.findByTierAndActiveTrue(t)
                        .orElseThrow());
    }
}