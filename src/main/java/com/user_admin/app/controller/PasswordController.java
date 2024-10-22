package com.user_admin.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class PasswordController {

    @GetMapping("/reset-password/{token}/{email}")
    public String showResetPasswordPage(@PathVariable String token, @PathVariable String email, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("email", email);
        return "reset_password";
    }

    @GetMapping("/activate-account/{activationToken}/{email}")
    public String showActivateAccountPage(@PathVariable String activationToken, @PathVariable String email, Model model) {
        model.addAttribute("activationToken", activationToken);
        model.addAttribute("email", email);
        return "activate_account";
    }
}
