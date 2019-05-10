package com.example.demo.demofeign.rateLimiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 限流逻辑类
 */
public class RateLimiter {

    private final long rateToMsConversion;

    private final AtomicInteger consumedTokens = new AtomicInteger();
    private final AtomicLong lastRefillTime = new AtomicLong(0);

    @Deprecated
    public RateLimiter() {
        this(TimeUnit.SECONDS);
    }

    public RateLimiter(TimeUnit averageRateUnit) {
        switch (averageRateUnit) {
            case SECONDS:
                rateToMsConversion = 1000;
                break;
            case MINUTES:
                rateToMsConversion = 60 * 1000;
                break;
            default:
                throw new IllegalArgumentException("TimeUnit of " + averageRateUnit + " is not supported");
        }
    }

    public boolean acquire(int burstSize, long averageRate) {
        return acquire(burstSize, averageRate, System.currentTimeMillis());
    }

    public boolean acquire(int burstSize, long averageRate, long currentTimeMillis) {
        if (burstSize <= 0 || averageRate <= 0) { // Instead of throwing exception, we just let all the traffic go
            return true;
        }

        refillToken(burstSize, averageRate, currentTimeMillis);
        return consumeToken(burstSize);
    }

    /**
     *
     * @param burstSize
     * @param averageRate
     * @param currentTimeMillis 当前时间戳
     */
    private void refillToken(int burstSize, long averageRate, long currentTimeMillis) {
        long refillTime = lastRefillTime.get();
        long timeDelta = currentTimeMillis - refillTime;
        long newTokens = timeDelta * averageRate / rateToMsConversion;
        if (newTokens > 0) {
            long newRefillTime = refillTime == 0
                    ? currentTimeMillis
                    : refillTime + newTokens * rateToMsConversion / averageRate;
            if (lastRefillTime.compareAndSet(refillTime, newRefillTime)) {
                while (true) {
                    int currentLevel = consumedTokens.get();
                    int adjustedLevel = Math.min(currentLevel, burstSize); // In case burstSize decreased
                    int newLevel = (int) Math.max(0, adjustedLevel - newTokens);
                    if (consumedTokens.compareAndSet(currentLevel, newLevel)) {
                        return;
                    }
                }
            }
        }
    }

    private boolean consumeToken(int burstSize) {
        while (true) {
            int currentLevel = consumedTokens.get();
            if (currentLevel >= burstSize) {
                return false;
            }
            if (consumedTokens.compareAndSet(currentLevel, currentLevel + 1)) {
                return true;
            }
        }
    }

    public void reset() {
        consumedTokens.set(0);
        lastRefillTime.set(0);
    }
}
