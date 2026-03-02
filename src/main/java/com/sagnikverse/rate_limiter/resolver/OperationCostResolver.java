package com.sagnikverse.rate_limiter.resolver;

import org.springframework.stereotype.Component;

@Component
public class OperationCostResolver {

    public int resolveCost(String endpoint, String method) {

        if (endpoint.startsWith("/api/users") && method.equals("GET")) {
            return 1;
        }

        if (endpoint.startsWith("/api/search")) {
            return 5;
        }

        if (endpoint.startsWith("/api/reports/generate")) {
            return 50;
        }

        if (endpoint.startsWith("/api/ai/analyze")) {
            return 100;
        }

        return 1; // default cost
    }
}