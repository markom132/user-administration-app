package com.user_admin.app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Asynchronous service for sending emails.
 */
@Service
public class EmailAsyncService {

    // JavaMailSender used to send emails
    private final JavaMailSender mailSender;
    private final Logger logger = LoggerFactory.getLogger(EmailAsyncService.class);

    /**
     * Constructor to initialize EmailAsyncService with a JavaMailSender.
     *
     * @param mailSender The JavaMailSender to send emails.
     */
    public EmailAsyncService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an email asynchronously with the provided content and optional inline image.
     *
     * @param to              The recipient email address.
     * @param subject         The subject of the email.
     * @param htmlContent     The HTML content of the email.
     * @param inlineImagePath The path to the image to be embedded in the email (can be null if no image is needed).
     */
    @Async
    public void sendEmail(String to, String subject, String htmlContent, String inlineImagePath) {
        try {
            // Create a new MIME message for the email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Set email recipient, subject, and HTML content
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Add inline image if path is provided
            if (inlineImagePath != null) {
                ClassPathResource image = new ClassPathResource(inlineImagePath);
                helper.addInline("logoImage", image);  // Embeds the image with the given name
            }

            // Send the email asynchronously
            mailSender.send(message);
            logger.info("Email sent to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
