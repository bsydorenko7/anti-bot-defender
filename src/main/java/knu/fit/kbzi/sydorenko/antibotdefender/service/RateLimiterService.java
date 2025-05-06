package knu.fit.kbzi.sydorenko.antibotdefender.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_MILLIS = 10_000;

    private final Map<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        long now = Instant.now().toEpochMilli();
        requestTimestamps.putIfAbsent(ip, new ArrayDeque<>());

        Deque<Long> timestamps = requestTimestamps.get(ip);

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > TIME_WINDOW_MILLIS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }

            timestamps.addLast(now);
            return true;
        }
    }
}
