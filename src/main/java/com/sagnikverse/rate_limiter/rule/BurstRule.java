package com.sagnikverse.rate_limiter.rule;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.entity.BurstPolicy;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.resolver.BurstPolicyResolver;
import com.sagnikverse.rate_limiter.resolver.TimeWindowResolver;
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
    private final TimeWindowResolver timeWindowResolver;

    @Override
    public boolean supports(RequestContext context) {
        return context.getTier() != Tier.UNLIMITED;
    }
//no dependent on the burstRule, so removed it for performance
//    @Override
//    public boolean isAllowed(RequestContext context) {
//
//        BurstPolicy policy = burstResolver.resolve(context.getTier());
//
//        if (policy == null) {
//            return true;
//        }
//
//        String id = context.getIdentifier();
//
//        double hourlyCapacity = policy.getHourlyCapacity();
//        double hourlyRefill = policy.getHourlyRefill();
//
//        // 🔥 Apply time-based multiplier safely
//        if (timeWindowResolver.isPeak(context.getRequestTime())) {
//            hourlyCapacity *= timeWindowResolver.capacityMultiplier();
//            hourlyRefill *= timeWindowResolver.refillMultiplier();
//        }
//
//        boolean hourlyAllowed = bucketService.execute(
//                "burst:hour:" + id,
//                (int) hourlyCapacity,
//                hourlyRefill,
//                policy.getHourlyTtl()
//        );
//
//        if (!hourlyAllowed) {
//            return false;
//        }
//
//        return bucketService.execute(
//                "burst:5min:" + id,
//                policy.getBurstCapacity(),
//                policy.getBurstRefill(),
//                policy.getBurstTtl()
//        );
//    }

    @Override
    public boolean isAllowed(RequestContext context) {

        BurstPolicy policy = burstResolver.resolve(context.getTier());

        if (policy == null) return true;

        String id = context.getIdentifier();

        double hourlyCapacity = policy.getHourlyCapacity();
        double hourlyRefill = policy.getHourlyRefill();

        if (timeWindowResolver.isPeak(context.getRequestTime())) {
            hourlyCapacity *= 0.5;
            hourlyRefill *= 0.5;
        }

        boolean hourlyAllowed = bucketService.execute(
                "burst:hour:" + id,
                (int) hourlyCapacity,
                hourlyRefill,
                policy.getHourlyTtl(),
                context.getCost()
        );

        if (!hourlyAllowed) return false;

        return bucketService.execute(
                "burst:5min:" + id,
                policy.getBurstCapacity(),
                policy.getBurstRefill(),
                policy.getBurstTtl(),
                context.getCost()
        );
    }
}