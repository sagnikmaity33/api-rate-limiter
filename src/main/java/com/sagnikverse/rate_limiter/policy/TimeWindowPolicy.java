package com.sagnikverse.rate_limiter.policy;

public record TimeWindowPolicy(
        int startHour,
        int endHour,
        double capacityMultiplier,
        double refillMultiplier
) {}