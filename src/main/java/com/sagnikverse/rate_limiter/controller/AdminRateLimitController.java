package com.sagnikverse.rate_limiter.controller;

import com.sagnikverse.rate_limiter.entity.*;
import com.sagnikverse.rate_limiter.repository.*;
import com.sagnikverse.rate_limiter.service.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin APIs for managing rate limit system.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminRateLimitController {

    private final SubscriptionService subscriptionService;
    private final ReportService reportService;
    private final TokenBucketRepository bucketRepository;

    /**
     * Assign tier to identifier.
     */
    @PostMapping("/tier/assign")
    public Subscription assignTier(@RequestBody Subscription request) {

        return subscriptionService
                .assignTier(request.getIdentifier(),
                        request.getTier());
    }

    /**
     * Reset token bucket manually.
     */
    @PostMapping("/bucket/reset/{identifier}")
    public String resetBucket(@PathVariable String identifier) {

        bucketRepository.deleteAll(
                bucketRepository.findByIdentifier(identifier)
                        .stream()
                        .toList()
        );

        return "Bucket reset for " + identifier;
    }

    /**
     * Get tier usage report.
     */
    @GetMapping("/report/tier/{tier}")
    public Map<String, Object> getTierReport(
            @PathVariable Tier tier) {

        return reportService.tierReport(tier);
    }

    /**
     * Get all active buckets.
     */
    @GetMapping("/buckets")
    public List<TokenBucket> getAllBuckets() {

        return bucketRepository.findAll();
    }
}