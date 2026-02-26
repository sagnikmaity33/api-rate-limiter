package com.sagnikverse.rate_limiter.service;



/**
 * Abstraction for rate limiting strategy.
 * Can be DB-based or Redis-based.
 */
public interface RateLimiterService {

    boolean allowRequest(RequestContext context);
}