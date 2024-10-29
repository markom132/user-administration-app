package com.user_admin.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling password reset and account activation views
 */
@Controller
@RequestMapping("/api")
public class PasswordController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);

    /**
     * Displays the reset password page.
     *
     * @param token    the reset token used for password verification
     * @param email    the user's email address
     * @param jwtToken the JWT token for authentication (for REST API which will be called to reset password)
     * @param model    the model to hold attributes for the view
     * @return the name of the reset password view
     */
    @GetMapping("/reset-password/{token}/{email}/{jwtToken}")
    public String showResetPasswordPage(@PathVariable String token,
                                        @PathVariable String email,
                                        @PathVariable String jwtToken,
                                        Model model) {
        // Adding attributes to the model for use in the reset password view
        model.addAttribute("token", token);
        model.addAttribute("email", email);
        model.addAttribute("jwtToken", jwtToken);

        logger.info("Reset password page requested for email: {}", email);
        return "reset_password";
    }

    /**
     * Displays the account activation page.
     *
     * @param activationToken the token used for account activation
     * @param email           the user's email address
     * @param jwtToken        the JWT token for authentication
     * @param model           the model to hold attributes for the view
     * @return tha name of the account activation view
     */
    @GetMapping("/activate-account/{activationToken}/{email}/{jwtToken}")
    public String showActivateAccountPage(@PathVariable String activationToken,
                                          @PathVariable String email,
                                          @PathVariable String jwtToken,
                                          Model model) {
        // Adding attributes to the model for use in the account activation view
        model.addAttribute("activationToken", activationToken);
        model.addAttribute("email", email);
        model.addAttribute("jwtToken", jwtToken);

        logger.info("Account activation page requested for email: {}", email);
        return "activate_account";
    }
}
