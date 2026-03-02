package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.*;
import com.sagnikverse.rate_limiter.repository.AccessControlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final AccessControlRepository repository;
    private final StringRedisTemplate redisTemplate;

    private String whitelistKey(String id) {
        return "whitelist:" + id;
    }

    private String blacklistKey(String id) {
        return "blacklist:" + id;
    }

    /**
     * Runtime fast check (NO DB)
     */
    public AccessType checkAccess(String identifier) {

        if (Boolean.TRUE.equals(
                redisTemplate.hasKey(blacklistKey(identifier)))) {
            return AccessType.BLACKLIST;
        }

        if (Boolean.TRUE.equals(
                redisTemplate.hasKey(whitelistKey(identifier)))) {
            return AccessType.WHITELIST;
        }

        return null;
    }

    /**
     * Add entry (persistent + Redis)
     */
    public AccessControlEntry addEntry(AccessControlEntry entry) {

        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());

        AccessControlEntry saved = repository.save(entry);

        String key = entry.getType() == AccessType.WHITELIST
                ? whitelistKey(entry.getIdentifier())
                : blacklistKey(entry.getIdentifier());

        if (entry.getExpiresAt() != null) {

            long seconds = Duration.between(
                    LocalDateTime.now(),
                    entry.getExpiresAt()
            ).getSeconds();

            redisTemplate.opsForValue()
                    .set(key, "1", Duration.ofSeconds(seconds));
        } else {
            redisTemplate.opsForValue()
                    .set(key, "1");
        }

        return saved;
    }

    /**
     * Remove entry
     */
    public void removeEntry(String identifier) {

        repository.deleteByIdentifier(identifier);

        redisTemplate.delete(whitelistKey(identifier));
        redisTemplate.delete(blacklistKey(identifier));
    }

    /**
     * Sync DB → Redis (for startup recovery)
     */
    public void preloadToRedis() {

        repository.findAll().forEach(entry -> {

            if (entry.getExpiresAt() != null &&
                    entry.getExpiresAt().isBefore(LocalDateTime.now())) {
                return;
            }

            String key = entry.getType() == AccessType.WHITELIST
                    ? whitelistKey(entry.getIdentifier())
                    : blacklistKey(entry.getIdentifier());

            redisTemplate.opsForValue().set(key, "1");
        });
    }
}