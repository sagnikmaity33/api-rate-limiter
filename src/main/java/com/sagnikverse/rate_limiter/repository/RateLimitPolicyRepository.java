package com.sagnikverse.rate_limiter.repository;

import com.sagnikverse.rate_limiter.entity.RateLimitPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateLimitPolicyRepository
        extends JpaRepository<RateLimitPolicy, Long> {

    Optional<RateLimitPolicy>
    findByIdentifierTypeAndActiveTrue(String identifierType);
}
