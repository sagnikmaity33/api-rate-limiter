package com.sagnikverse.rate_limiter.config;

import com.sagnikverse.rate_limiter.entity.*;
import com.sagnikverse.rate_limiter.repository.RateLimitPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RateLimitPolicyRepository policyRepository;

    @Override
    public void run(String... args) {

        createPolicyIfNotExists(Tier.FREE, 100, 1);
        createPolicyIfNotExists(Tier.PRO, 1000, 5);
        createPolicyIfNotExists(Tier.ENTERPRISE, 5000, 10);
        createPolicyIfNotExists(Tier.UNLIMITED, 999999, 1000);
    }

    private void createPolicyIfNotExists(Tier tier,
                                         int capacity,
                                         int refillRate) {

        policyRepository.findByTierAndActiveTrue(tier)
                .orElseGet(() ->
                        policyRepository.save(
                                RateLimitPolicy.builder()
                                        .tier(tier)
                                        .capacity(capacity)
                                        .refillRate((double) refillRate)
                                        .active(true)
                                        .build()
                        )
                );
    }
}