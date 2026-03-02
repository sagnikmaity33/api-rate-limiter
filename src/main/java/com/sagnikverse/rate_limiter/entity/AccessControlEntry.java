package com.sagnikverse.rate_limiter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_control")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessControlEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String identifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessType type; // WHITELIST / BLACKLIST

    private LocalDateTime expiresAt; // null = permanent

    private String reason;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}