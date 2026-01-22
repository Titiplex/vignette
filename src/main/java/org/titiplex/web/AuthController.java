package org.titiplex.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.titiplex.api.dto.RegisterForm;
import org.titiplex.service.UserService;

@Controller
public class AuthController {
    @Value("${spring.application.name}")
    String appName;
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("appName", appName);
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm("", "", ""));
        model.addAttribute("appName", appName);
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("form") RegisterForm form, Model model) {
        try {
            userService.register(form.username(), form.email(), form.password());
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
