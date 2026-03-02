package com.sagnikverse.rate_limiter.rule;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.service.BucketExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class IpUserCombinedRule implements RateLimitRule {

    private final BucketExecutionService bucketService;

    @Override
    public boolean supports(RequestContext context) {
        return context.getUserId() != null;
    }

    @Override
    public boolean isAllowed(RequestContext context) {

        boolean userAllowed = bucketService.execute(
                "user:" + context.getUserId(),
                1000,
                1000.0 / 3600,
                3600,
                context.getCost()
        );

        if (!userAllowed) return false;

        return bucketService.execute(
                "ip:" + context.getIdentifier(),
                2000,
                2000.0 / 3600,
                3600,context.getCost()

        );
    }
}