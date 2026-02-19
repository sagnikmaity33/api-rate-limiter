package com.sagnikverse.rate_limiter.repository;

import com.sagnikverse.rate_limiter.entity.TokenBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenBucketRepository
        extends JpaRepository<TokenBucket, Long> {

    Optional<TokenBucket> findByIdentifier(String identifier);
}
