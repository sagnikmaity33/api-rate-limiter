package com.sagnikverse.rate_limiter.rule;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.entity.RateLimitPolicy;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.repository.RateLimitPolicyRepository;
import com.sagnikverse.rate_limiter.service.BucketExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
public class TierRule implements RateLimitRule {

    private final BucketExecutionService bucketService;
    private final RateLimitPolicyRepository policyRepository;

    @Override
    public boolean supports(RequestContext context) {
        return context.getTier() != Tier.UNLIMITED;
    }

    @Override
    public boolean isAllowed(RequestContext context) {

        RateLimitPolicy policy = policyRepository
                .findByTierAndActiveTrue(context.getTier())
                .orElseThrow();

        String key = "tier:" + context.getIdentifier();

        return bucketService.execute(
                key,
                policy.getCapacity(),
                policy.getRefillRate()
        );
    }
}