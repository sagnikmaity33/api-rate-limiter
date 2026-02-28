package com.sagnikverse.rate_limiter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_bucket")
@Data
public class TokenBucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String identifier;

    private Double tokens;

    private Double capacity;

    private Double refillRate;

    private LocalDateTime lastRefill;
}
