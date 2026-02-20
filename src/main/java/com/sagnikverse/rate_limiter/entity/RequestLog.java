package com.sagnikverse.rate_limiter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores every request decision.
 * Used for analytics and reporting.
 */
@Entity
@Table(name = "request_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String identifier;

    @Enumerated(EnumType.STRING)
    private Tier tier;

    private Boolean allowed;

    private LocalDateTime timestamp;
}