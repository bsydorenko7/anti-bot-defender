package knu.fit.kbzi.sydorenko.antibotdefender.controller;

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
    public String handleFormSubmission(@RequestParam String username,
                                       @RequestParam(name = "hidden_field", required = false) String hiddenField) {
        if (hiddenField != null && !hiddenField.isBlank()) {
            return "Bot detected! Submission rejected.";
        }
        return "Hello, " + username + "! Submission accepted.";
    }
}