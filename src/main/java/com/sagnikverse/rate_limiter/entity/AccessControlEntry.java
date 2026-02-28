package com.sagnikverse.rate_limiter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "access_control")
@Data
public class AccessControlEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String identifier;

    @Enumerated(EnumType.STRING)
    private AccessType type; // WHITELIST / BLACKLIST
}