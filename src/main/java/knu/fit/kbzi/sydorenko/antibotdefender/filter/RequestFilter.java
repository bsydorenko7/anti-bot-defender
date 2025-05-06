package knu.fit.kbzi.sydorenko.antibotdefender.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import knu.fit.kbzi.sydorenko.antibotdefender.entity.BlacklistedIp;
import knu.fit.kbzi.sydorenko.antibotdefender.repository.BlacklistedIpRepository;
import knu.fit.kbzi.sydorenko.antibotdefender.util.AllowedUserAgents;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestURI = request.getRequestURI();

        log.info("Request to [{}] from IP [{}] with User-Agent [{}]", requestURI, ipAddress, userAgent);

        if (blacklistedIpRepository.existsByIpAddress(ipAddress)) {
            log.warn("Blocked IP [{}] for request [{}]", ipAddress, requestURI);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your IP address is blocked");
            return;
        }

        if (!AllowedUserAgents.isAllowed(userAgent)) {
            blacklistedIpRepository.save(
                    BlacklistedIp.builder()
                            .ipAddress(ipAddress)
                            .reason("Suspicious User-Agent: " + userAgent)
                            .blockedAt(LocalDateTime.now())
                            .build()
            );

            log.warn("Blocked User-Agent [{}] from IP [{}]", userAgent, ipAddress);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Suspicious User-Agent detected");
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
