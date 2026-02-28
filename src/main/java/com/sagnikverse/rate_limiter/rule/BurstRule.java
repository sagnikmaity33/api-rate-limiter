package com.sagnikverse.rate_limiter.rule;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.entity.BurstPolicy;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.resolver.BurstPolicyResolver;
import com.sagnikverse.rate_limiter.service.BucketExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class BurstRule implements RateLimitRule {

    private final BucketExecutionService bucketService;
    private final BurstPolicyResolver burstResolver;

    @Override
    public boolean supports(RequestContext context) {
        return context.getTier() != Tier.UNLIMITED;
    }

    @Override
    public boolean isAllowed(RequestContext context) {

        BurstPolicy policy = burstResolver.resolve(context.getTier());

        if (policy == null) {
            return true;
        }

        String id = context.getIdentifier();

        boolean hourlyAllowed = bucketService.execute(
                "burst:hour:" + id,
                policy.getHourlyCapacity(),
                policy.getHourlyRefill(),
                policy.getHourlyTtl()
        );

        if (!hourlyAllowed) {
            return false;
        }

        return bucketService.execute(
                "burst:5min:" + id,
                policy.getBurstCapacity(),
                policy.getBurstRefill(),
                policy.getBurstTtl()
        );
    }
}