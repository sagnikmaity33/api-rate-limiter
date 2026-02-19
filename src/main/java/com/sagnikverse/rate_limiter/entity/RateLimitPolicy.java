package com.sagnikverse.rate_limiter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "rate_limit_policy")
@Data
public class RateLimitPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String identifierType; // USER / IP / API_KEY

    private Integer capacity;

    private Integer refillRate;

    private Boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}
