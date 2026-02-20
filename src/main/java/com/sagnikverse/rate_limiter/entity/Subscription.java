package com.sagnikverse.rate_limiter.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Maps identifier (USER:123 or API_KEY:xyz)
 * to a subscription tier.
 */
@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Example: USER:123
    @Column(unique = true)
    private String identifier;

    @Enumerated(EnumType.STRING)
    private Tier tier;

    private LocalDateTime updatedAt;
}