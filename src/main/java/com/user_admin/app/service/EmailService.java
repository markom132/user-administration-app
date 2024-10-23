package com.user_admin.app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Reset Your Password");

            String htmlContent = loadEmailTemplate("password_reset_template.html");

            htmlContent = htmlContent.replace("{{resetLink}}", resetLink);

            helper.setText(htmlContent, true);

            ClassPathResource image = new ClassPathResource("static/password.png");
            helper.addInline("logoImage", image);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset password email", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendActivateAccountEmail(String to, String activateLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Activate Your Account");

            String htmlContent = loadEmailTemplate("activate_account_template.html");

            htmlContent = htmlContent.replace("{{activateLink}}", activateLink);

            helper.setText(htmlContent, true);

            ClassPathResource image = new ClassPathResource("static/user.png");
            helper.addInline("logoImage", image);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset password email", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String loadEmailTemplate(String templateName) throws IOException {
        InputStream resource = new ClassPathResource("templates/" + templateName).getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        }
    }
}
