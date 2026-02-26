package com.sagnikverse.rate_limiter.rule;


import com.sagnikverse.rate_limiter.engine.RequestContext;

public class RateLimitRule {

    boolean supports(RequestContext context);
    boolean isAllowed(RequestContext context);
}
