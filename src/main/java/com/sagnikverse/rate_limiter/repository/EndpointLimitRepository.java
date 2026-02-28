package com.sagnikverse.rate_limiter.repository;

import com.sagnikverse.rate_limiter.entity.EndpointLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EndpointLimitRepository
        extends JpaRepository<EndpointLimit, Long> {

    Optional<EndpointLimit> findByMethodAndPath(String method, String path);
}