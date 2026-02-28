package com.sagnikverse.rate_limiter.service;


import com.sagnikverse.rate_limiter.engine.RequestContext;

/**
 * Abstraction for rate limiting strategy.
 * Can be DB-based or Redis-based.
 */
public interface RateLimiterService {

    boolean allowRequest(RequestContext context);
}