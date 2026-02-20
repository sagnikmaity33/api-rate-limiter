package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final RequestLogRepository requestLogRepository;

    /**
     * Returns tier-wise usage report.
     */
    public Map<String, Object> tierReport(Tier tier) {

        long total = requestLogRepository.countByTier(tier);
        long allowed = requestLogRepository.countByTierAndAllowedTrue(tier);
        long blocked = requestLogRepository.countByTierAndAllowedFalse(tier);

        Map<String, Object> report = new HashMap<>();
        report.put("tier", tier);
        report.put("totalRequests", total);
        report.put("allowedRequests", allowed);
        report.put("blockedRequests", blocked);

        return report;
    }
}