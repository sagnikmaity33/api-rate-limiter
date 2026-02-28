package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final RequestLogRepository repository;

    public Map<String, Object> overviewToday() {

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();

        Object[] data = repository.overviewSince(start);

        long total = (long) data[0];
        long allowed = data[1] == null ? 0 : ((Number) data[1]).longValue();
        long blocked = data[2] == null ? 0 : ((Number) data[2]).longValue();

        double successRate = total == 0 ? 0 :
                (allowed * 100.0) / total;

        return Map.of(
                "totalRequests", total,
                "allowed", allowed,
                "blocked", blocked,
                "successRatePercent", successRate
        );
    }
}