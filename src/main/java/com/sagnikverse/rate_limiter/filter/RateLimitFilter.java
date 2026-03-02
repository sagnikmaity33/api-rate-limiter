package com.sagnikverse.rate_limiter.filter;

import com.sagnikverse.rate_limiter.engine.RequestContext;
import com.sagnikverse.rate_limiter.entity.RequestLog;
import com.sagnikverse.rate_limiter.service.RateLimiterService;
import com.sagnikverse.rate_limiter.service.RequestLogService;
import com.sagnikverse.rate_limiter.service.SubscriptionService;
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
                .requestTime(LocalDateTime.now())
                .tier(subscriptionService.getTier(identifier))
                .build();

        boolean allowed = rateLimiterService.allowRequest(context);

        // Set headers
        response.setHeader("X-RateLimit-Tier", context.getTier().name());
        response.setHeader("X-RateLimit-Status",
                allowed ? "ALLOWED" : "BLOCKED");

        if (!allowed) {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");

            // Async log
            requestLogService.logAsync(
                    buildLog(context, false)
            );
            return;
        }

        // Async log
        //commneted for benchmark
//        requestLogService.logAsync(
//                buildLog(context, true)
//        );

        // IMPORTANT — Continue request flow
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