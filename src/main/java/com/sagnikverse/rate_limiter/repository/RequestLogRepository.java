package com.sagnikverse.rate_limiter.repository;

import com.sagnikverse.rate_limiter.entity.RequestLog;
import com.sagnikverse.rate_limiter.entity.Tier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;

public interface RequestLogRepository
        extends JpaRepository<RequestLog, Long> {

    List<RequestLog> findByIdentifier(String identifier);

    long countByTier(Tier tier);

    long countByTierAndAllowedTrue(Tier tier);

    long countByTierAndAllowedFalse(Tier tier);

    @Query("""
SELECT COUNT(r),
       SUM(CASE WHEN r.allowed = true THEN 1 ELSE 0 END),
       SUM(CASE WHEN r.allowed = false THEN 1 ELSE 0 END)
FROM RequestLog r
WHERE r.timestamp >= :start
""")
    Object[] overviewSince(LocalDateTime start);

    @Query("""
SELECT COUNT(DISTINCT r.identifier)
FROM RequestLog r
WHERE r.timestamp >= :start
""")
    Long uniqueIdentifiersSince(LocalDateTime start);

    @Query("""
SELECT r.endpoint, COUNT(r)
FROM RequestLog r
GROUP BY r.endpoint
ORDER BY COUNT(r) DESC
""")
    List<Object[]> mostRequestedEndpoints();


    @Query("""
SELECT r.identifier, COUNT(r)
FROM RequestLog r
WHERE r.allowed = false
GROUP BY r.identifier
ORDER BY COUNT(r) DESC
""")
    List<Object[]> topViolators(Pageable pageable);

    @Query("""
SELECT r.endpoint, COUNT(r)
FROM RequestLog r
WHERE r.allowed = false
GROUP BY r.endpoint
ORDER BY COUNT(r) DESC
""")
    List<Object[]> mostBlockedEndpoints();

    @Query("""
SELECT FUNCTION('DATE_TRUNC', 'minute', r.timestamp),
       COUNT(r)
FROM RequestLog r
GROUP BY FUNCTION('DATE_TRUNC', 'minute', r.timestamp)
ORDER BY FUNCTION('DATE_TRUNC', 'minute', r.timestamp)
""")
    List<Object[]> requestsPerMinute();



}