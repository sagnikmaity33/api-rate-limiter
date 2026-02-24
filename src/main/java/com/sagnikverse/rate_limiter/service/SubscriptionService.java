package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.Subscription;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.repository.SubscriptionRepository;
import com.sagnikverse.rate_limiter.repository.TokenBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final TokenBucketRepository tokenBucketRepository;

    /**
     * Returns tier of identifier.
     * Default tier = FREE if no subscription found.
     */
    public Tier getTier(String identifier) {

        Subscription sub = repository
                .findByIdentifier(identifier)
                .orElse(null);

        if (sub == null) {
            return Tier.FREE;
        }

        // Expiration check
        if (sub.getExpiresAt() != null &&
                sub.getExpiresAt().isBefore(LocalDateTime.now())) {

            sub.setTier(Tier.FREE);
            sub.setExpiresAt(null);
            repository.save(sub);

            // Invalidate Redis bucket
            redisTemplate.delete("bucket:" + identifier);

            // Invalidate DB bucket
            tokenBucketRepository.findByIdentifier(identifier)
                    .ifPresent(tokenBucketRepository::delete);

            return Tier.FREE;
        }

        return sub.getTier();
    }

    /**
     * Assign or upgrade tier.
     */
    public Subscription assignTier(String identifier, Tier tier) {

        Subscription sub = repository
                .findByIdentifier(identifier)
                .orElse(new Subscription());

        sub.setIdentifier(identifier);
        sub.setTier(tier);
        sub.setUpdatedAt(LocalDateTime.now());

        repository.save(sub);

        // Invalidate Redis bucket immediately
        redisTemplate.delete("bucket:" + identifier);

        // Invalidate DB bucket
        tokenBucketRepository.findByIdentifier(identifier)
                .ifPresent(tokenBucketRepository::delete);

        return sub;
    }
}