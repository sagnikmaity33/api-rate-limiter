package com.sagnikverse.rate_limiter.rule;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.service.BucketExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
@Order(3)
public class TimeBasedRule implements RateLimitRule {

    private final BucketExecutionService bucketService;

    @Override
    public boolean supports(RequestContext context) {

        int hour = context.getRequestTime().getHour();
        return hour >= 9 && hour <= 17;
    }

    @Override
    public boolean isAllowed(RequestContext context) {

        return bucketService.execute(
                "time:peak:" + context.getIdentifier(),
                50,
                50.0,
                3600
        );
    }
}