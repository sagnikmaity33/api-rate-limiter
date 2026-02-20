package com.sagnikverse.rate_limiter.repository;

import com.sagnikverse.rate_limiter.entity.TokenBucket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenBucketRepository
        extends JpaRepository<TokenBucket, Long> {


    Optional<TokenBucket> findByIdentifier(String identifier);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TokenBucket t WHERE t.identifier = :identifier")
    Optional<TokenBucket> findByIdentifierForUpdate(@Param("identifier") String identifier);
}
