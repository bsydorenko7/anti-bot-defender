package knu.fit.kbzi.sydorenko.antibotdefender.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FormController {

    @GetMapping("/form")
    public String showForm() {
        return "form";
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<String> handleFormSubmission(@RequestParam String username,
                                       @RequestParam(name = "hidden_field", required = false) String hiddenField) {
        if (hiddenField != null && !hiddenField.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bot detected! Submission rejected.");
        }
        return ResponseEntity.ok("Hello, " + username + "! Submission accepted.");
    }
}