package com.sagnikverse.rate_limiter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "endpoint_limits")
@Data
public class EndpointLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;
    private String path;

    private Integer capacity;
    private Double refillRate;
    private Integer ttl;
}