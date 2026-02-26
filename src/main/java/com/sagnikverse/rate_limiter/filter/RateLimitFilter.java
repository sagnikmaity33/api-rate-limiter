package com.sagnikverse.rate_limiter.filter;

import com.sagnikverse.rate_limiter.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.service.SubscriptionService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final SubscriptionService subscriptionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String identifier = extractIdentifier(request);

        RequestContext context = RequestContext.builder()
                .identifier(identifier)
                .endpoint(request.getRequestURI())
                .httpMethod(request.getMethod())
                .requestTime(java.time.LocalDateTime.now())
                .tier(subscriptionService.getTier(identifier))
                .build();

        boolean allowed = rateLimiterService.allowRequest(context);

        if (!allowed) {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractIdentifier(HttpServletRequest request) {
        return "IP:" + request.getRemoteAddr();
    }
}