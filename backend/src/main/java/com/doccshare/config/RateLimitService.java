package com.doccshare.config;

import com.doccshare.common.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    @Value("${doccshare.ratelimit.auth-attempts-per-minute:10}")
    private int authAttemptsPerMinute;

    @Value("${doccshare.ratelimit.download-requests-per-minute:30}")
    private int downloadRequestsPerMinute;

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> downloadBuckets = new ConcurrentHashMap<>();

    public void checkAuthRateLimit(String ip) {
        Bucket bucket = authBuckets.computeIfAbsent(ip, k -> createBucket(authAttemptsPerMinute, Duration.ofMinutes(1)));
        consume(bucket, "Too many authentication attempts. Please try again later.");
    }

    public void checkDownloadRateLimit(String key) {
        Bucket bucket = downloadBuckets.computeIfAbsent(key, k -> createBucket(downloadRequestsPerMinute, Duration.ofMinutes(1)));
        consume(bucket, "Too many download requests for this link. Please try again later.");
    }

    private Bucket createBucket(int capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillDuration)
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void consume(Bucket bucket, String errorMessage) {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            long waitForRefillNanos = probe.getNanosToWaitForRefill();
            long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(waitForRefillNanos));
            throw new RateLimitExceededException(errorMessage, retryAfterSeconds);
        }
    }
}

