package com.sagnikverse.rate_limiter.service;

import com.sagnikverse.rate_limiter.entity.RequestLog;
import com.sagnikverse.rate_limiter.entity.Tier;
import com.sagnikverse.rate_limiter.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;

//@Service
//@RequiredArgsConstructor
//public class RequestLogService {
//
//    private final RequestLogRepository repository;
//
//    public void log(String identifier,
//                    String endpoint,
//                    Tier tier,
//                    boolean allowed) {
//
//        RequestLog log = RequestLog.builder()
//                .identifier(identifier)
//                .endpoint(endpoint)
//                .tier(tier)
//                .allowed(allowed)
//                .timestamp(LocalDateTime.now())
//                .build();
//
//        repository.save(log);
//    }
//}


@Service
@RequiredArgsConstructor
public class RequestLogService {

    private final RequestLogRepository repository;
    private final Executor analyticsExecutor;

    public void logAsync(RequestLog log) {
        analyticsExecutor.execute(() -> {
            try {
                repository.save(log);
            } catch (Exception e) {
                // Never break request flow
                System.err.println("Async logging failed: " + e.getMessage());
            }
        });
    }
}