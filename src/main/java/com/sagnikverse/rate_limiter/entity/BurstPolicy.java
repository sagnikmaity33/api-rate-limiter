package com.sagnikverse.rate_limiter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BurstPolicy {

    private Integer hourlyCapacity;
    private Double hourlyRefill;

    private Integer burstCapacity;
    private Double burstRefill;

    private Integer hourlyTtl;
    private Integer burstTtl;
}