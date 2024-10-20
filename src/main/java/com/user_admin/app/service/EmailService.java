package com.user_admin.app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(to);
            helper.setSubject("Reset Your Password");

            String htmlContent = "<!DOCTYPE html>" +
                    "<html lang='en'>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "<title>Reset Your Password</title>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                    ".container { background-color: #ffffff; max-width: 600px; margin: 20px auto; padding: 20px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }" +
                    "h1 { color: #333333; }" +
                    "p { color: #666666; font-size: 16px; }" +
                    "a.button { background-color: #4CAF50; color: white; padding: 10px 20px; text-align: center; text-decoration: none; display: inline-block; font-size: 16px; border-radius: 5px; }" +
                    "a.button:hover { background-color: #45a049; }" +
                    ".footer { margin-top: 20px; font-size: 12px; color: #999999; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h1>Password Reset Request</h1>" +
                    "<p>Hello,</p>" +
                    "<p>We received a request to reset your password. Click the button below to reset it.</p>" +
                    "<a href='" + resetLink + "' class='button'>Reset Password</a>" +
                    "<p>If you did not request a password reset, please ignore this email.</p>" +
                    "<div class='footer'>" +
                    "<p>&copy; 2024 Your Company. All rights reserved.</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

}
