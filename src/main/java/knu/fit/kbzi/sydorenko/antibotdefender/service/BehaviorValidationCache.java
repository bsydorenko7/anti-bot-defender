package knu.fit.kbzi.sydorenko.antibotdefender.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class BehaviorValidationCache {

    private final Cache<String, Boolean> validatedIps;

    public BehaviorValidationCache() {
        this.validatedIps = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build();
    }

    public void markValidated(String ip) {
        validatedIps.put(ip, true);
    }

    public boolean isRecentlyValidated(String ip) {
        return validatedIps.getIfPresent(ip) != null;
    }
}