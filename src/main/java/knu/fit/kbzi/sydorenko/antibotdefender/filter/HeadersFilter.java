package knu.fit.kbzi.sydorenko.antibotdefender.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import knu.fit.kbzi.sydorenko.antibotdefender.repository.BlacklistRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class HeadersFilter implements Filter {

    private static final List<String> SUSPICIOUS_USER_AGENTS = Arrays.asList(
            "curl", "wget", "python-requests", "PostmanRuntime", "scrapy"
    );
    private static final List<String> TRUSTED_DOMAINS = Arrays.asList(
            "example.com", "trusted-site.com"
    );

    private final BlacklistRepository blacklistRepository;

    public HeadersFilter(BlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String userAgent = httpRequest.getHeader("User-Agent");
            String referer = httpRequest.getHeader("Referer");
            String origin = httpRequest.getHeader("Origin");
            String ipAddress = httpRequest.getRemoteAddr();

            if (userAgent != null && isSuspiciousUserAgent(userAgent)) {
                log.warn("Suspicious User-Agent detected: {} from IP: {}", userAgent, ipAddress);
                blacklistRepository.banIp(ipAddress);
            }
            if (referer == null || referer.isEmpty() || !isTrustedDomain(referer)) {
                log.warn("Invalid or missing Referer header: {} from IP: {}", referer, ipAddress);
                blacklistRepository.banIp(ipAddress);
            }
            if (origin != null && !isTrustedDomain(origin)) {
                log.warn("Invalid Origin header: {} from IP: {}", origin, ipAddress);
                blacklistRepository.banIp(ipAddress);
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isSuspiciousUserAgent(String userAgent) {
        return SUSPICIOUS_USER_AGENTS.stream().anyMatch(userAgent.toLowerCase()::contains);
    }

    private boolean isTrustedDomain(String url) {
        return TRUSTED_DOMAINS.stream().anyMatch(url::contains);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}