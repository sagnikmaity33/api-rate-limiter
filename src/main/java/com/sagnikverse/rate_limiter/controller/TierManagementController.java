package com.sagnikverse.rate_limiter.controller;

import com.sagnikverse.rate_limiter.entity.Subscription;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tier")
@RequiredArgsConstructor
public class TierManagementController {

    private final SubscriptionService subscriptionService;

    /**
     * Assign tier to identifier.
     * Example:
     * POST /admin/tier/assign
     * {
     *   "identifier": "USER:123",
     *   "tier": "PRO"
     * }
     */
    @PostMapping("/assign")
    public Subscription assignTier(@RequestBody Subscription request) {

        return subscriptionService
                .assignTier(request.getIdentifier(),
                        request.getTier());
    }
}
