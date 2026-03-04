
# 🚀 Distributed API Rate Limiter & Policy Enforcement Engine  
### Production-Grade Spring Boot Backend System

---

## 📌 Project Summary

This project is a **distributed, rule-driven, cost-aware API rate limiting and access control system** built using **Spring Boot, Redis, and PostgreSQL**.

It evolved in multiple Git phases to demonstrate:

- Progressive architectural design
- Concurrency-safe distributed systems thinking
- Performance optimization
- Clean separation of concerns
- Production-readiness mindset

This is not a simple rate limiter.  
It is a **policy enforcement engine** designed for SaaS-grade backend systems.

---

# 🎯 Problem Statement

Modern APIs must handle:

- API abuse
- Uneven traffic spikes
- Multi-tier user plans
- Expensive operations (AI, report generation)
- Fair resource distribution
- Horizontal scaling
- Concurrency safety
- Resilience against infrastructure failures

This project solves all of the above using layered architecture.

---

# 🏗 System Architecture Overview

```
Client Request
     ↓
Access Control Layer (Whitelist / Blacklist)
     ↓
User Circuit Breaker (CLOSED / OPEN / HALF_OPEN)
     ↓
RateLimitFilter
     ↓
RuleEngineService
     ↓
List<RateLimitRule>
     ↓
BucketExecutionService
     ↓
Redis Token Bucket (Lua, atomic)
     ↓
DB Fallback (Emergency Mode)
     ↓
Postgres Persistence
```

---

# 🧱 Core Architectural Principles

| Principle | Implementation |
|------------|---------------|
| Separation of concerns | Layered services |
| Composition over conditionals | RuleEngine |
| No DB in hot path | Redis + Caffeine |
| Distributed safety | Lua atomic scripts |
| Horizontal scalability | Stateless app + Redis |
| Graceful degradation | Circuit breaker |
| Observability | Custom response headers |
| Resilience | Resilience4j integration |

---

# 🔥 1️⃣ Access Control Layer (Security First)

## Purpose

Hard enforcement before rate limiting.

### Features

- Permanent blacklist
- Permanent whitelist
- Temporary whitelist/blacklist with expiration
- CRUD management APIs
- Import/export ready
- Redis runtime cache
- DB as source of truth

### Runtime Behavior

On each request:

1. Check Redis key:
   - `blacklist:{identifier}` → block immediately
   - `whitelist:{identifier}` → bypass rate limits
2. No DB call in request path.

### Why This Layer Exists

Security policies should not depend on rate limiting.  
This ensures malicious users are blocked instantly.

---

# 🔒 2️⃣ User-Level Circuit Breaker

Protects system from repeated violators.

## Circuit States

| State      | Behavior |
|------------|----------|
| CLOSED     | Normal rate limiting |
| OPEN       | All requests blocked |
| HALF_OPEN  | Limited recovery testing |

## Escalation Strategy

| Strike Count | Block Duration |
|--------------|----------------|
| 1            | 5 minutes |
| 2            | 15 minutes |
| 3            | 1 hour |
| 4+           | 24 hours |

## Internal Storage

Redis keys:

```
circuit:{identifier}
strike:{identifier}
halfopen:{identifier}
```

## How It Works

- On repeated 429 responses → recordFailure()
- Circuit transitions to OPEN
- TTL handles expiration
- On next request after expiry → HALF_OPEN
- Limited test requests allowed
- If successful → CLOSED
- If failed → OPEN with escalation

## Why Redis?

- Distributed-safe
- TTL-based state management
- No locking
- Horizontal scaling friendly

---

# ⚙️ 3️⃣ Rule Engine (Policy Composition)

Instead of writing nested if-else logic, rules are modular.

```java
public interface RateLimitRule {
    boolean supports(RequestContext context);
    boolean isAllowed(RequestContext context);
}
```

### Implemented Rules

- TierRule
- BurstRule
- EndpointRule
- IP + User combined rule
- Time-based adjustments

### Execution Model

```
for (rule : rules) {
    if (rule.supports(context)) {
        if (!rule.isAllowed(context)) {
            return false;
        }
    }
}
```

Rules are AND-composed.

---

# 📊 4️⃣ Tier-Based Rate Limiting

Users belong to subscription tiers:

- FREE
- PRO
- ENTERPRISE
- UNLIMITED

Policies stored in DB:

```
rate_limit_policy
subscriptions
```

## Policy Caching

Caffeine cache:

- Avoid DB in hot path
- TTL-based refresh
- Automatic invalidation on upgrade

---

# ⚡ 5️⃣ Burst Limiting

Supports:

- Hourly bucket
- 5-minute burst bucket

Prevents:

- Short spikes overwhelming backend
- Abuse via high-frequency bursts

Uses Redis Lua for atomic refill + deduction.

---

# 🕒 6️⃣ Time-Based Adjustments

Peak hour multiplier:

- During peak → reduce capacity
- During off-peak → full capacity

Implemented via policy adjustment, not separate bucket stacking.

---

# 💰 7️⃣ Cost-Based (Weighted) Rate Limiting

Instead of:

```
1 request = 1 token
```

We implemented:

```
1 request = N tokens
```

### Example Cost Table

| Endpoint | Cost |
|-----------|------|
| GET /users | 1 |
| GET /search | 5 |
| POST /reports/generate | 50 |
| POST /ai/analyze | 100 |

### Budget Example

PRO tier → 1000 points/hour

User can:
- Make 1000 simple reads
- Or 200 searches
- Or 20 reports
- Or any mix

## Internal Implementation

Redis Lua script deducts `cost` atomically:

```
if tokens >= cost then
   tokens = tokens - cost
```

Ensures:

- Atomicity
- Concurrency safety
- Fair resource usage

---

# 🔥 8️⃣ Redis Token Bucket (Atomic Design)

Lua script handles:

- Fetch tokens
- Calculate refill
- Deduct cost
- Update last_refill
- Set TTL
- Return allowed / blocked

### Why Lua?

Without Lua:

- Race conditions
- Double deduction
- Concurrency bugs

With Lua:

- Single atomic operation
- No locking required
- Safe under high concurrency

---

# 🗄 9️⃣ DB Fallback Strategy

If Redis becomes unavailable:

- Circuit breaker activates
- Fallback to Postgres
- PESSIMISTIC_WRITE locking
- Slower but safe

### Tradeoff

| Advantage | Disadvantage |
|------------|--------------|
| Resilience | Not scalable under heavy concurrency |
| Data integrity | Lock contention possible |

Designed for emergency only.

---

# 📈 Concurrency Testing

Performed using:

```
hey -n 50000 -c 500
```

Results:

- ~10,000 requests/sec
- Stable P95 latency
- No race condition
- No duplicate token deduction

---

# 🧪 Complex Scenario Testing

### 1. Mixed Cost Requests

- 1 cost 1
- 1 cost 5
- 1 cost 50

Verified proportional deduction.

### 2. Circuit Escalation

Triggered repeated 429 → observed OPEN state → tested HALF_OPEN recovery.

### 3. Whitelist Override

Added to whitelist → bypassed all rate limiting.

### 4. Blacklist Enforcement

Immediate 403 → no Redis bucket access.

### 5. Tier Upgrade

On tier change:
- Redis keys invalidated
- Bucket recreated with new capacity

---

# 🐳 Deployment Architecture

Dockerized stack:

- Spring Boot app
- Redis
- PostgreSQL

All in isolated Docker network.

Supports horizontal scaling by replicating app containers.

---

# 📡 Observability

Response headers:

```
X-RateLimit-Tier
X-RateLimit-Status
X-Circuit-State
X-Access-Control
```

Helps debugging and monitoring.

---

# 📌 Use Cases

- SaaS API monetization
- AI API cost control
- Multi-tenant platforms
- Enterprise API governance
- Preventing brute-force attacks
- Protecting expensive compute endpoints

---

# ⚖️ Pros & Cons

## ✅ Pros

- Distributed-safe
- Atomic token deduction
- Modular rule engine
- Cost-aware fairness
- Production-ready
- Horizontal scalability
- No DB in hot path
- Resilient to Redis failure

## ❌ Cons

- DB fallback not highly scalable
- Lua scripts add complexity
- Rule stacking can increase complexity
- Requires Redis infrastructure

---

# 🧠 Lessons Learned

- Atomicity matters more than algorithm speed.
- DB should never be in request hot path.
- Caching is mandatory for scale.
- Fallback strategies must consider lock contention.
- Layered architecture simplifies complexity.
- Method signature changes ripple across system.
- Concurrency must be tested intentionally.

---

# 🎯 Interview Talking Points

1. Why Redis over in-memory?
2. Why Lua instead of MULTI/EXEC?
3. How would you scale globally?
4. How to prevent DB fallback meltdown?
5. How to implement sliding window?
6. How cost-based model enables billing?
7. Tradeoffs between token bucket and sliding window?

---

# 🚀 Future Enhancements

- Sliding window algorithm
- Cost analytics dashboard
- Billing integration
- CIDR IP blocking
- Kafka audit logs
- Global distributed limiter (multi-region)

---

# 📌 Conclusion

This system demonstrates:

- Distributed systems thinking
- Concurrency-safe design
- Policy-driven architecture
- Resilient backend engineering
- Production-level Spring Boot expertise

It evolved step-by-step via Git commits to demonstrate structured problem-solving and architectural progression.

---

**Built for backend interviews to demonstrate real-world production design, not just feature implementation.**
