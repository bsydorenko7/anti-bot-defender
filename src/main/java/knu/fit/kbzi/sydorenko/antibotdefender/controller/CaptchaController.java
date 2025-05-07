package knu.fit.kbzi.sydorenko.antibotdefender.controller;

import knu.fit.kbzi.sydorenko.antibotdefender.service.CaptchaService;
import knu.fit.kbzi.sydorenko.antibotdefender.service.IpBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CaptchaController {

    private final IpBlockService ipBlockService;
    private final CaptchaService captchaService;

    @Value("${captcha.site-key}")
    private String captchaSiteKey;

    @GetMapping("/captcha")
    public String showCaptchaForm(Model model, @RequestParam String ip, @RequestParam String reason) {
        model.addAttribute("ip", ip);
        model.addAttribute("reason", reason);
        model.addAttribute("siteKey", captchaSiteKey);
        return "captcha";
    }

    @PostMapping("/captcha/verify")
    public String verifyCaptcha(
            @RequestParam String ip,
            @RequestParam String reason,
            @RequestParam("g-recaptcha-response") String captchaToken
    ) {
        if (!captchaService.verify(captchaToken, ip)) {
            ipBlockService.blockIfNotExists(ip, reason);
            return "redirect:/captcha-failed";
        }

        return "redirect:/form";
    }
}


