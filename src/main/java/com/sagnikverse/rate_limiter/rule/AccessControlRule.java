package com.sagnikverse.rate_limiter.rule;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.entity.AccessType;
import com.sagnikverse.rate_limiter.repository.AccessControlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(0) // Highest priority
public class AccessControlRule implements RateLimitRule {

    private final AccessControlRepository repository;

    @Override
    public boolean supports(RequestContext context) {
        return true;
    }

    @Override
    public boolean isAllowed(RequestContext context) {

        return repository.findByIdentifier(context.getIdentifier())
                .map(entry -> entry.getType() != AccessType.BLACKLIST)
                .orElse(true);
    }
}