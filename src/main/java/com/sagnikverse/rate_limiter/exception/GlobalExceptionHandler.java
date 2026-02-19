package com.sagnikverse.rate_limiter.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(
            RateLimitExceededException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "Too Many Requests");
        body.put("message", "Rate limit exceeded");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Limit",
                String.valueOf(ex.getLimit()));
        headers.add("X-RateLimit-Remaining",
                String.valueOf(ex.getRemaining()));
        headers.add("Retry-After",
                String.valueOf(ex.getRetryAfterSeconds()));

        return new ResponseEntity<>(
                body,
                headers,
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
