package knu.fit.kbzi.sydorenko.antibotdefender.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class CaptchaService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${captcha.secret}")
    private String captchaSecret;

    private static final String CAPTCHA_API_URL = "https://www.google.com/recaptcha/api/siteverify";

    public boolean verify(String token, String ip) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("secret", captchaSecret);
        requestBody.add("response", token);
        requestBody.add("remoteip", ip);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    CAPTCHA_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> response = responseEntity.getBody();
            boolean success = Boolean.TRUE.equals(response.get("success"));
            log.info("CAPTCHA verification result: {}", success);
            return success;
        } catch (Exception e) {
            log.warn("CAPTCHA error: {}", e.getMessage());
            return false;
        }
    }
}
