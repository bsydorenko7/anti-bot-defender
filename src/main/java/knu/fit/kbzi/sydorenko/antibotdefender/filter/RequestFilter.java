package knu.fit.kbzi.sydorenko.antibotdefender.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import knu.fit.kbzi.sydorenko.antibotdefender.entity.BlacklistedIp;
import knu.fit.kbzi.sydorenko.antibotdefender.repository.BlacklistedIpRepository;
import knu.fit.kbzi.sydorenko.antibotdefender.service.RateLimiterService;
import knu.fit.kbzi.sydorenko.antibotdefender.util.RequestHeaderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class RequestFilter extends OncePerRequestFilter {

    private final BlacklistedIpRepository blacklistedIpRepository;
    private final RequestHeaderValidator requestHeaderValidator;
    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = getClientIpAddress(request);
        String requestURI = request.getRequestURI();

        log.info("Request to [{}] from IP [{}]", requestURI, ipAddress);

        if (blacklistedIpRepository.existsByIpAddress(ipAddress)) {
            log.warn("Blocked IP [{}] for request [{}]", ipAddress, requestURI);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your IP address is blocked");
            return;
        }

        String blockingReason = requestHeaderValidator.validateHeaders(request);
        if (blockingReason != null) {
            log.warn("Blocked IP [{}] due to [{}]", ipAddress, blockingReason);

            blacklistedIpRepository.save(
                    BlacklistedIp.builder()
                            .ipAddress(ipAddress)
                            .reason(blockingReason)
                            .blockedAt(LocalDateTime.now())
                            .build()
            );

            response.sendError(HttpServletResponse.SC_FORBIDDEN, blockingReason);
            return;
        }

        if (!rateLimiterService.isAllowed(ipAddress)) {
            blacklistedIpRepository.save(
                    BlacklistedIp.builder()
                            .ipAddress(ipAddress)
                            .reason("Rate limit exceeded")
                            .blockedAt(LocalDateTime.now())
                            .build()
            );
            log.warn("Blocked IP [{}] due to rate limit", ipAddress);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Rate limit exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
