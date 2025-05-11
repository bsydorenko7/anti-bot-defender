package knu.fit.kbzi.sydorenko.antibotdefender.controller;

import jakarta.servlet.http.HttpServletRequest;
import knu.fit.kbzi.sydorenko.antibotdefender.service.IpBlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static knu.fit.kbzi.sydorenko.antibotdefender.filter.RequestFilter.getClientIpAddress;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BehaviorController {

    private final IpBlockService ipBlockService;

    @PostMapping("/behavior")
    @ResponseBody
    public ResponseEntity<Void> handleBehavior(@RequestBody BehaviorPayload payload, HttpServletRequest request) {
        String ip = getClientIpAddress(request);

        if (payload.headless() || !payload.hasMouseMove() || !payload.hasKeyPress()) {
            ipBlockService.blockIfNotExists(ip, "Suspicious behavior: " + payload);
            return ResponseEntity.status(403).build();

        }
        return ResponseEntity.ok().build();
    }

    public record BehaviorPayload(boolean hasMouseMove, boolean hasKeyPress, boolean headless) {}
}
