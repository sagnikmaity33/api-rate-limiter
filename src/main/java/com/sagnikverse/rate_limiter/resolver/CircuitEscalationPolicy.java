package com.sagnikverse.rate_limiter.resolver;

import org.springframework.stereotype.Component;

@Component
public class CircuitEscalationPolicy {

    public long blockDurationSeconds(int strikes) {

        return switch (strikes) {
            case 1 -> 300;        // 5 minutes
            case 2 -> 900;        // 15 minutes
            case 3 -> 3600;       // 1 hour
            default -> 86400;     // 24 hours
        };
    }

    public int halfOpenAllowedRequests() {
        return 10;
    }
}