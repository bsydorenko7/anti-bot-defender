package knu.fit.kbzi.sydorenko.antibotdefender.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import knu.fit.kbzi.sydorenko.antibotdefender.util.AllowedUserAgents;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class RequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String requestURI = request.getRequestURI();

        logger.info("Request to [{}] from IP [{}] with User-Agent [{}]", requestURI, ipAddress, userAgent);

        if (!AllowedUserAgents.isAllowed(userAgent)) {
            logger.warn("Blocked non-whitelisted User-Agent [{}] from IP [{}]", userAgent, ipAddress);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Suspicious User-Agent detected");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
