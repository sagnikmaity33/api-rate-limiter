package com.sagnikverse.rate_limiter.rule;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.service.BucketExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class EndpointRule implements RateLimitRule {

    private final BucketExecutionService bucketService;

    @Override
    public boolean supports(RequestContext context) {
        return context.getEndpoint().equals("/api/search");
    }

    @Override
    public boolean isAllowed(RequestContext context) {

        return bucketService.execute(
                "endpoint:/api/search:" + context.getIdentifier(),
                10,
                10
        );
    }
}