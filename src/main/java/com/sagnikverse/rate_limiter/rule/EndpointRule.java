package com.sagnikverse.rate_limiter.rule;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.entity.EndpointLimit;
import com.sagnikverse.rate_limiter.repository.EndpointLimitRepository;
import com.sagnikverse.rate_limiter.rule.RateLimitRule;
import com.sagnikverse.rate_limiter.service.BucketExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(3)
public class EndpointRule implements RateLimitRule {

    private final BucketExecutionService bucketService;
    private final EndpointLimitRepository repository;

    @Override
    public boolean supports(RequestContext context) {
        return repository
                .findByMethodAndPath(
                        context.getHttpMethod(),
                        context.getEndpoint())
                .isPresent();
    }

    @Override
    public boolean isAllowed(RequestContext context) {

        EndpointLimit limit = repository
                .findByMethodAndPath(
                        context.getHttpMethod(),
                        context.getEndpoint())
                .orElseThrow();

        return bucketService.execute(
                "endpoint:" + context.getEndpoint() + ":" + context.getIdentifier(),
                limit.getCapacity(),
                limit.getRefillRate(),
                limit.getTtl(),
                context.getCost()
        );
    }
}