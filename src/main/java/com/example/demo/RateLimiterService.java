package com.example.demo;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class RateLimiterService {

    private final RateLimitRepository repository;
    private static final int MAX_TOKENS = 5;
    private static final long REFILL_DURATION_MS = 60000; // 1 minute

    public RateLimiterService(RateLimitRepository repository) {
        this.repository = repository;
    }

    public synchronized boolean tryConsume(String key) {
        long now = System.currentTimeMillis();
        Optional<RateLimit> optLimit = repository.findById(key);

        if (optLimit.isEmpty()) {
            repository.save(new RateLimit(key, MAX_TOKENS - 1, now));
            return true;
        }

        RateLimit limit = optLimit.get();
        long timePassed = now - limit.getLastRefillTime();

        if (timePassed > REFILL_DURATION_MS) {
            limit.setTokens(MAX_TOKENS);
            limit.setLastRefillTime(now);
        }

        if (limit.getTokens() > 0) {
            limit.setTokens(limit.getTokens() - 1);
            repository.save(limit);
            return true;
        }

        return false;
    }
}
