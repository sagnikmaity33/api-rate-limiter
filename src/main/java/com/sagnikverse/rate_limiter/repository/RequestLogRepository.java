package com.sagnikverse.rate_limiter.repository;

import com.sagnikverse.rate_limiter.entity.RequestLog;
import com.sagnikverse.rate_limiter.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestLogRepository
        extends JpaRepository<RequestLog, Long> {

    List<RequestLog> findByIdentifier(String identifier);

    long countByTier(Tier tier);

    long countByTierAndAllowedTrue(Tier tier);

    long countByTierAndAllowedFalse(Tier tier);
}