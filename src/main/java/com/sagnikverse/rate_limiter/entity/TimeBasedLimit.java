package com.sagnikverse.rate_limiter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "time_based_limits")
@Data
public class TimeBasedLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer startHour;
    private Integer endHour;

    private Integer capacity;
    private Double refillRate;
    private Integer ttl;
}