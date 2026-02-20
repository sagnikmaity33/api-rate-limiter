package com.sagnikverse.rate_limiter.repository;

import com.sagnikverse.rate_limiter.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdentifier(String identifier);
}
