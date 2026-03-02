package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.CircuitState;
import com.sagnikverse.rate_limiter.resolver.CircuitEscalationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserCircuitBreakerService {

    private final StringRedisTemplate redisTemplate;
    private final CircuitEscalationPolicy escalationPolicy;

    private String circuitKey(String id) {
        return "circuit:" + id;
    }

    private String strikeKey(String id) {
        return "strike:" + id;
    }

    private String halfOpenKey(String id) {
        return "halfopen:" + id;
    }

    /**
     * Check circuit state before processing request.
     */
    public CircuitState getState(String identifier) {

        String state = redisTemplate.opsForValue()
                .get(circuitKey(identifier));

        if (state == null) {
            return CircuitState.CLOSED;
        }

        return CircuitState.valueOf(state);
    }

    /**
     * Called when request was rate limited.
     */
    public void recordFailure(String identifier) {

        CircuitState state = getState(identifier);

        // If HALF_OPEN fails → immediately reopen with escalation
        if (state == CircuitState.HALF_OPEN) {
            reopenWithEscalation(identifier);
            return;
        }

        Long strikes = redisTemplate.opsForValue()
                .increment(strikeKey(identifier));

        int strikeCount = strikes != null ? strikes.intValue() : 1;

        long blockSeconds =
                escalationPolicy.blockDurationSeconds(strikeCount);

        redisTemplate.opsForValue().set(
                circuitKey(identifier),
                CircuitState.OPEN.name(),
                Duration.ofSeconds(blockSeconds)
        );
    }

    /**
     * Called when request was allowed.
     */
    public void recordSuccess(String identifier) {

        CircuitState state = getState(identifier);

        if (state == CircuitState.HALF_OPEN) {
            // Successful recovery → fully close
            closeCircuit(identifier);
        }
    }

    /**
     * Transition OPEN → HALF_OPEN when TTL expires.
     */
    public void transitionToHalfOpen(String identifier) {

        redisTemplate.opsForValue().set(
                circuitKey(identifier),
                CircuitState.HALF_OPEN.name(),
                Duration.ofSeconds(60) // half-open window
        );

        redisTemplate.opsForValue().set(
                halfOpenKey(identifier),
                "0",
                Duration.ofSeconds(60)
        );
    }

    /**
     * Allow limited test requests in HALF_OPEN state.
     */
    public boolean allowHalfOpenRequest(String identifier) {

        Long count = redisTemplate.opsForValue()
                .increment(halfOpenKey(identifier));

        int maxAllowed = escalationPolicy.halfOpenAllowedRequests();

        return count != null && count <= maxAllowed;
    }

    private void reopenWithEscalation(String identifier) {

        Long strikes = redisTemplate.opsForValue()
                .increment(strikeKey(identifier));

        int strikeCount = strikes != null ? strikes.intValue() : 1;

        long blockSeconds =
                escalationPolicy.blockDurationSeconds(strikeCount);

        redisTemplate.opsForValue().set(
                circuitKey(identifier),
                CircuitState.OPEN.name(),
                Duration.ofSeconds(blockSeconds)
        );

        redisTemplate.delete(halfOpenKey(identifier));
    }

    private void closeCircuit(String identifier) {

        redisTemplate.delete(circuitKey(identifier));
        redisTemplate.delete(strikeKey(identifier));
        redisTemplate.delete(halfOpenKey(identifier));
    }

    public void manualUnblock(String identifier) {
        closeCircuit(identifier);
    }
}