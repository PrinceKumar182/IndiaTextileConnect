package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rate_limits")
public class RateLimit {
    @Id
    private String key; // usually IP or User ID
    private int tokens;
    private long lastRefillTime;

    public RateLimit() {}

    public RateLimit(String key, int tokens, long lastRefillTime) {
        this.key = key;
        this.tokens = tokens;
        this.lastRefillTime = lastRefillTime;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public int getTokens() { return tokens; }
    public void setTokens(int tokens) { this.tokens = tokens; }
    public long getLastRefillTime() { return lastRefillTime; }
    public void setLastRefillTime(long lastRefillTime) { this.lastRefillTime = lastRefillTime; }
}
