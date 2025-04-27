package knu.fit.kbzi.sydorenko.antibotdefender.util;

import java.util.Set;

public class AllowedUserAgents {

    private static final Set<String> ALLOWED_PATTERNS = Set.of("mozilla", "chrome", "firefox", "safari",
            "edge", "opera", "samsungbrowser", "ucbrowser"
    );

    public static boolean isAllowed(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return false;
        }

        String lowerCaseAgent = userAgent.toLowerCase();
        return ALLOWED_PATTERNS.stream()
                .anyMatch(lowerCaseAgent::contains);
    }
}
