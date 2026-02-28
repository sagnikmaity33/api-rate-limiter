package com.sagnikverse.rate_limiter.resolver;

import com.sagnikverse.rate_limiter.entity.BurstPolicy;
import com.sagnikverse.rate_limiter.entity.Tier;
import org.springframework.stereotype.Component;

@Component
public class BurstPolicyResolver {

    public BurstPolicy resolve(Tier tier) {

        switch (tier) {

            case FREE:
                return new BurstPolicy(
                        100,
                        100.0 / 3600.0,
                        120,
                        120.0 / 300.0,
                        3600,
                        300
                );

            case PRO:
                return new BurstPolicy(
                        1000,
                        1000.0 / 3600.0,
                        1500,
                        1500.0 / 300.0,
                        3600,
                        300
                );

            case ENTERPRISE:
                return new BurstPolicy(
                        10000,
                        10000.0 / 3600.0,
                        20000,
                        20000.0 / 300.0,
                        3600,
                        300
                );

            case UNLIMITED:
            default:
                return null;
        }
    }
}