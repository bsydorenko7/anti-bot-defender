package knu.fit.kbzi.sydorenko.antibotdefender.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class RequestHeaderValidator {

    private static final Set<String> ALLOWED_USER_AGENT_PATTERNS = Set.of(
            "mozilla", "chrome", "firefox", "safari", "edge", "opera", "samsungbrowser", "ucbrowser"
    );

    @Value("${security.expected-host}")
    private String expectedHost;

    private static final Set<String> METHODS_REQUIRING_HEADER_VALIDATION = Set.of(
            "POST", "PUT", "DELETE", "PATCH"
    );

    public String validateHeaders(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");

        log.info("Header check: User-Agent='{}', Referer='{}', Origin='{}'", userAgent, referer, origin);

        if (userAgent == null || userAgent.isBlank()) {
            return "Missing or empty User-Agent";
        }

        String lowerAgent = userAgent.toLowerCase();
        boolean isValidAgent = ALLOWED_USER_AGENT_PATTERNS.stream().anyMatch(lowerAgent::contains);
        if (!isValidAgent) {
            return "Suspicious User-Agent: " + userAgent;
        }

        String method = request.getMethod().toUpperCase();
        if (METHODS_REQUIRING_HEADER_VALIDATION.contains(method)) {
            if (referer == null || !referer.contains(expectedHost)) {
                return "Missing or invalid Referer: " + referer;
            }

            if (origin == null || !origin.contains(expectedHost)) {
                return "Missing or invalid Origin: " + origin;
            }
        }

        return null;
    }
}

