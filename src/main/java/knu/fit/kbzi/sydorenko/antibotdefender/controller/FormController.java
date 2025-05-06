package knu.fit.kbzi.sydorenko.antibotdefender.controller;

import jakarta.servlet.http.HttpServletRequest;
import knu.fit.kbzi.sydorenko.antibotdefender.entity.BlacklistedIp;
import knu.fit.kbzi.sydorenko.antibotdefender.repository.BlacklistedIpRepository;
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

    private final BlacklistedIpRepository blacklistedIpRepository;

    @GetMapping("/form")
    public String showForm() {
        return "form";
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<String> handleFormSubmission(
            @RequestParam String username,
            @RequestParam(name = "hidden_field", required = false) String hiddenField,
            HttpServletRequest request
    ) {
        String ipAddress = getClientIpAddress(request);

        if (hiddenField != null && !hiddenField.isBlank()) {
            if (!blacklistedIpRepository.existsByIpAddress(ipAddress)) {
                blacklistedIpRepository.save(
                        BlacklistedIp.builder()
                                .ipAddress(ipAddress)
                                .reason("Honeypot field triggered (form submission)")
                                .blockedAt(LocalDateTime.now())
                                .build()
                );
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bot detected! Submission rejected.");
        }

        return ResponseEntity.ok("Hello, " + username + "! Submission accepted.");
    }
}