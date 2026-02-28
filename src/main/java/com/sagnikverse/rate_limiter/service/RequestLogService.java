package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.RequestLog;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestLogService {

    private final RequestLogRepository repository;

    public void log(String identifier,
                    String endpoint,
                    Tier tier,
                    boolean allowed) {

        RequestLog log = RequestLog.builder()
                .identifier(identifier)
                .endpoint(endpoint)
                .tier(tier)
                .allowed(allowed)
                .timestamp(LocalDateTime.now())
                .build();

        repository.save(log);
    }
}