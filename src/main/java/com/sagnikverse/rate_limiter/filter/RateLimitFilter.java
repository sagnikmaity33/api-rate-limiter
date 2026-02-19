package com.sagnikverse.rate_limiter.filter;

import com.sagnikverse.rate_limiter.exception.RateLimitExceededException;
import com.sagnikverse.rate_limiter.resolver.IdentifierResolver;
import com.sagnikverse.rate_limiter.service.TokenBucketService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final IdentifierResolver resolver;
    private final TokenBucketService service;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String identifier = resolver.resolve(request);

        try {
            service.allowRequest(identifier);

        } catch (RateLimitExceededException ex) {

            response.setStatus(429);
            response.setContentType("application/json");

            response.setHeader("X-RateLimit-Limit",
                    String.valueOf(ex.getLimit()));
            response.setHeader("X-RateLimit-Remaining",
                    String.valueOf(ex.getRemaining()));
            response.setHeader("Retry-After",
                    String.valueOf(ex.getRetryAfterSeconds()));

            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded\"}"
            );

            return; // IMPORTANT → stop filter chain
        }

        filterChain.doFilter(request, response);
    }
}
