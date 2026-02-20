package com.sagnikverse.rate_limiter.entity;

/**
 * Represents subscription tiers.
 * Each tier will map to a different rate limit policy.
 */
public enum Tier {
    FREE,
    PRO,
    ENTERPRISE,
    UNLIMITED
}