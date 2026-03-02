package com.sagnikverse.rate_limiter.resolver;

import com.sagnikverse.rate_limiter.policy.TimeWindowPolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TimeWindowResolver {

    private static final TimeWindowPolicy PEAK =
            new TimeWindowPolicy(9, 17, 0.5, 0.5);

    public boolean isPeak(LocalDateTime time) {
        int hour = time.getHour();
        return hour >= PEAK.startHour() && hour <= PEAK.endHour();
    }

    public double capacityMultiplier() {
        return PEAK.capacityMultiplier();
    }

    public double refillMultiplier() {
        return PEAK.refillMultiplier();
    }
}