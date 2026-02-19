package com.sagnikverse.rate_limiter.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class IdentifierResolver {

    public String resolve(HttpServletRequest request) {

        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null) return "API_KEY:" + apiKey;

        String userId = request.getHeader("X-User-Id");
        if (userId != null) return "USER:" + userId;

        return "IP:" + request.getRemoteAddr();
    }
}

