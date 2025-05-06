package knu.fit.kbzi.sydorenko.antibotdefender.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import knu.fit.kbzi.sydorenko.antibotdefender.service.IpBlockService;
import knu.fit.kbzi.sydorenko.antibotdefender.service.RateLimiterService;
import knu.fit.kbzi.sydorenko.antibotdefender.util.RequestHeaderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class RequestFilter extends OncePerRequestFilter {

    private final IpBlockService ipBlockService;
    private final RequestHeaderValidator requestHeaderValidator;
    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = getClientIpAddress(request);
        String requestURI = request.getRequestURI();

        log.info("Request to [{}] from IP [{}]", requestURI, ipAddress);

        if (ipBlockService.isBlacklisted(ipAddress)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your IP address is blocked");
            return;
        }

        String blockingReason = requestHeaderValidator.validateHeaders(request);
        if (blockingReason != null) {
            ipBlockService.blockIfNotExists(ipAddress, blockingReason);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, blockingReason);
            return;
        }

        if (!rateLimiterService.isAllowed(ipAddress)) {
            ipBlockService.blockIfNotExists(ipAddress, "Rate limit exceeded");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Rate limit exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader == null) ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }
}
