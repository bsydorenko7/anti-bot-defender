package knu.fit.kbzi.sydorenko.antibotdefender.service;

import knu.fit.kbzi.sydorenko.antibotdefender.entity.BlacklistedIp;
import knu.fit.kbzi.sydorenko.antibotdefender.repository.BlacklistedIpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IpBlockService {

    private final BlacklistedIpRepository repository;

    public void blockIfNotExists(String ipAddress, String reason) {
        if (!repository.existsByIpAddress(ipAddress)) {
            log.info("Blocking IP [{}] for reason: {}", ipAddress, reason);
            repository.save(
                    BlacklistedIp.builder()
                            .ipAddress(ipAddress)
                            .reason(reason)
                            .blockedAt(LocalDateTime.now())
                            .build()
            );
        }
    }

    public boolean isBlacklisted(String ipAddress) {
        return repository.existsByIpAddress(ipAddress);
    }
}
