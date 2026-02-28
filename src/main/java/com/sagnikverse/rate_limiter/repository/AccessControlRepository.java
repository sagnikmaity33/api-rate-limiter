package com.sagnikverse.rate_limiter.repository;
import com.sagnikverse.rate_limiter.entity.AccessControlEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessControlRepository
        extends JpaRepository<AccessControlEntry, Long> {

    Optional<AccessControlEntry> findByIdentifier(String identifier);
}