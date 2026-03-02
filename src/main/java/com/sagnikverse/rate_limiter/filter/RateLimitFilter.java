package com.sagnikverse.rate_limiter.filter;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.entity.AccessType;
import com.sagnikverse.rate_limiter.entity.CircuitState;
import com.sagnikverse.rate_limiter.entity.RequestLog;
import com.sagnikverse.rate_limiter.service.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final SubscriptionService subscriptionService;
    private final RequestLogService requestLogService;
    private final UserCircuitBreakerService circuitBreakerService;
    private final AccessControlService accessControlService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String identifier = extractIdentifier(request);

        AccessType accessType =
                accessControlService.checkAccess(identifier);

        if (accessType == AccessType.BLACKLIST) {
            response.setStatus(403);
            response.setHeader("X-Access-Control", "BLACKLISTED");
            response.getWriter().write("Access permanently blocked");
            return;
        }

        if (accessType == AccessType.WHITELIST) {
            response.setHeader("X-Access-Control", "WHITELISTED");
            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 1️⃣ Check Circuit State
        CircuitState state = circuitBreakerService.getState(identifier);

        if (state == CircuitState.OPEN) {
            response.setStatus(429);
            response.setHeader("X-Circuit-State", "OPEN");
            response.getWriter().write("User temporarily blocked due to excessive violations");
            return;
        }

        if (state == CircuitState.HALF_OPEN) {

            boolean allowedTest =
                    circuitBreakerService.allowHalfOpenRequest(identifier);

            if (!allowedTest) {
                response.setStatus(429);
                response.setHeader("X-Circuit-State", "HALF_OPEN_BLOCK");
                response.getWriter().write("Half-open test limit reached");
                return;
            }
        }

        // 🔹 2️⃣ Build Context
        RequestContext context = RequestContext.builder()
                .identifier(identifier)
                .endpoint(request.getRequestURI())
                .httpMethod(request.getMethod())
                .requestTime(LocalDateTime.now())
                .tier(subscriptionService.getTier(identifier))
                .build();

        // 🔹 3️⃣ Evaluate Rate Limit Rules
        boolean allowed = rateLimiterService.allowRequest(context);

        response.setHeader("X-RateLimit-Tier", context.getTier().name());
        response.setHeader("X-RateLimit-Status", allowed ? "ALLOWED" : "BLOCKED");

        // 🔹 4️⃣ Handle Violation
        if (!allowed) {

            circuitBreakerService.recordFailure(identifier);

            response.setStatus(429);
            response.getWriter().write("Too Many Requests");

            requestLogService.logAsync(buildLog(context, false));
            return;
        }

        // 🔹 5️⃣ Handle Success
        circuitBreakerService.recordSuccess(identifier);

        filterChain.doFilter(request, response);
    }

    private RequestLog buildLog(RequestContext context, boolean allowed) {
        return RequestLog.builder()
                .identifier(context.getIdentifier())
                .endpoint(context.getEndpoint())
                .tier(context.getTier())
                .allowed(allowed)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String extractIdentifier(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return "IP:" + forwarded.split(",")[0].trim();
        }
        return "IP:" + request.getRemoteAddr();
    }
}