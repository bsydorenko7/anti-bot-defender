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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
            sendForbidden(response, "Your IP address is blocked");
            return;
        }

        String blockingReason = requestHeaderValidator.validateHeaders(request);
        if (blockingReason != null) {
            ipBlockService.blockIfNotExists(ipAddress, blockingReason);
            sendForbidden(response, blockingReason);
            return;
        }

        if (!rateLimiterService.isAllowed(ipAddress)) {
            redirectToCaptcha(response, ipAddress, "Rate limit exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader == null) ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }

    private void redirectToCaptcha(HttpServletResponse response, String ip, String reason) throws IOException {
        String url = "/captcha?ip=" + URLEncoder.encode(ip, StandardCharsets.UTF_8)
                + "&reason=" + URLEncoder.encode(reason, StandardCharsets.UTF_8);
        response.sendRedirect(url);
    }
}
