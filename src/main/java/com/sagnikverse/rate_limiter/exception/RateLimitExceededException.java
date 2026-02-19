package com.sagnikverse.rate_limiter.exception;

public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;
    private final int limit;
    private final double remaining;

    public RateLimitExceededException(long retryAfterSeconds,
                                      int limit,
                                      double remaining) {
        this.retryAfterSeconds = retryAfterSeconds;
        this.limit = limit;
        this.remaining = remaining;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public int getLimit() {
        return limit;
    }

    public double getRemaining() {
        return remaining;
    }
}