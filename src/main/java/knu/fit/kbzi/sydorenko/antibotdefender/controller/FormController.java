package knu.fit.kbzi.sydorenko.antibotdefender.controller;

import jakarta.servlet.http.HttpServletRequest;
import knu.fit.kbzi.sydorenko.antibotdefender.entity.BlacklistedIp;
import knu.fit.kbzi.sydorenko.antibotdefender.repository.BlacklistedIpRepository;
import knu.fit.kbzi.sydorenko.antibotdefender.service.IpBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

import static knu.fit.kbzi.sydorenko.antibotdefender.filter.RequestFilter.getClientIpAddress;

@Controller
@RequiredArgsConstructor
public class FormController {

    private final IpBlockService ipBlockService;

    private static final long MIN_FILL_TIME_MS = 800;

    @GetMapping("/form")
    public String showForm() {
        return "form";
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<String> handleFormSubmission(
            @RequestParam String username,
            @RequestParam(name = "hidden_field", required = false) String hiddenField,
            @RequestParam(name = "form_created_at", required = false) Long formCreatedAt,
            HttpServletRequest request
    ) {
        String ipAddress = getClientIpAddress(request);

        if (hiddenField != null && !hiddenField.isBlank()) {
            ipBlockService.blockIfNotExists(ipAddress, "Honeypot field triggered");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bot detected! Submission rejected.");
        }

        if (formCreatedAt != null) {
            long now = System.currentTimeMillis();
            long delta = now - formCreatedAt;

            if (delta < MIN_FILL_TIME_MS) {
                ipBlockService.blockIfNotExists(ipAddress, "Form submitted too quickly (" + delta + " ms)");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Form submitted too quickly. Potential bot.");
            }
        }

        return ResponseEntity.ok("Hello, " + username + "! Submission accepted.");
    }
}