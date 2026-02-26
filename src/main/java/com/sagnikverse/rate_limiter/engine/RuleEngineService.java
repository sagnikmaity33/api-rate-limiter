package com.sagnikverse.rate_limiter.engine;

import com.sagnikverse.rate_limiter.rule.RateLimitRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleEngineService {

    private final List<RateLimitRule> rules;

    public boolean evaluate(RequestContext context) {

        for (RateLimitRule rule : rules) {

            if (rule.supports(context)) {

                if (!rule.isAllowed(context)) {
                    return false;
                }
            }
        }

        return true;
    }
}