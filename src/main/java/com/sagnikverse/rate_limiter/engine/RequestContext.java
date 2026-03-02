package com.sagnikverse.rate_limiter.engine;

import com.sagnikverse.rate_limiter.entity.Tier;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RequestContext {

    private String identifier;
    private String endpoint;
    private String httpMethod;
    private LocalDateTime requestTime;
    private Tier tier;
    private String userId;

    private int cost;
}