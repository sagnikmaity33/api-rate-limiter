package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.Subscription;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository repository;

    /**
     * Returns tier of identifier.
     * Default tier = FREE if no subscription found.
     */
    public Tier getTier(String identifier) {

        return repository.findByIdentifier(identifier)
                .map(Subscription::getTier)
                .orElse(Tier.FREE);
    }

    /**
     * Assign or upgrade tier.
     */
    public Subscription assignTier(String identifier, Tier tier) {

        Subscription subscription = repository
                .findByIdentifier(identifier)
                .orElse(new Subscription());

        subscription.setIdentifier(identifier);
        subscription.setTier(tier);

        return repository.save(subscription);
    }
}
