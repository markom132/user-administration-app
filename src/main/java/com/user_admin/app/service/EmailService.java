package com.user_admin.app.service;

import com.user_admin.app.model.dto.ErrorDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for sending emails related to user account management and error notifications.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a password reset email to the specific address.
     *
     * @param to        the recipient's email address
     * @param resetLink the link to reset the password
     */
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
            logger.info("Reset password email sent to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send reset password email: {}", e.getMessage());
            throw new RuntimeException("Failed to send reset password email", e);
        } catch (IOException e) {
            logger.error("I/O error while sending reset password email: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends an account activation email to the specific address.
     *
     * @param to           the recipient's email address
     * @param activateLink the link to activate the account
     */
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
            logger.info("Account activation email sent to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send activate account email: {}", e.getMessage());
            throw new RuntimeException("Failed to send reset password email", e);
        } catch (IOException e) {
            logger.error("I/O error while sending activate account email: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends an error notification email to the specified address.
     *
     * @param to       the recipient's email address
     * @param errorDTO the error information to include in the email
     * @throws MessagingException if an error occurs while creating email
     * @throws IOException        if an I/O error occurs while loading the template
     */
    public void sendErrorEmail(String to, ErrorDTO errorDTO) throws MessagingException, IOException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Error occurred");

            String htmlContent = loadEmailTemplate("error_occurred_template.html");
            htmlContent = htmlContent.replace("{{createdAt}}", errorDTO.getCreatedAt());
            htmlContent = htmlContent.replace("{{user_id}}", errorDTO.getUserEmail());
            htmlContent = htmlContent.replace("{{api}}", errorDTO.getApi());
            htmlContent = htmlContent.replace("{{request}}", errorDTO.getRequest());
            htmlContent = htmlContent.replace("{{message}}", errorDTO.getMessage());
            htmlContent = htmlContent.replace("{{codeLine}}", String.valueOf(errorDTO.getCodeLine()));

            StringBuilder entryParamsTable = new StringBuilder();
            for (Map.Entry<String, Object> entry : errorDTO.getEntryParams().entrySet()) {
                entryParamsTable.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
            }

            htmlContent = htmlContent.replace("{{entryParamsTable}}", entryParamsTable.toString());
            helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("Error email send to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send error occurred email: {}", e.getMessage());
            throw new RuntimeException("Failed to send error occurred email", e);
        } catch (IOException e) {
            logger.error("I/O error while sending error occurred email: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends an error notification email based on the provided error details.
     *
     * @param error       the throwable error that occurred
     * @param request     the HTTP request where the error occurred
     * @param entryParams optional parameters to include in the email
     * @throws MessagingException if an error occurs while creating the email
     * @throws IOException        if an I/O error occurs while loading the template
     */
    public void sendError(Throwable error, HttpServletRequest request, Optional<Map<String, Object>> entryParams) throws MessagingException, IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedCreatedAt = LocalDateTime.now().format(formatter);

        Map<String, Object> params = Map.of();

        if (entryParams.isPresent()) {
            params = entryParams.get();
        }

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCreatedAt(formattedCreatedAt);

        if (!params.isEmpty()) {
            errorDTO.setUserEmail((String) params.get("email"));
        } else {
            String queryParam1 = request.getParameter("email");
            errorDTO.setUserEmail(queryParam1);
        }

        errorDTO.setApi(request.getRequestURI());
        errorDTO.setRequest(request.getMethod());
        errorDTO.setMessage(error.getMessage());

        if (error.getStackTrace().length > 0) {
            StackTraceElement errorLocation = error.getStackTrace()[0];
            String className = errorLocation.getClassName();
            int errorLine = errorLocation.getLineNumber();
            errorDTO.setCodeLine(className + " " + errorLine);
        }

        errorDTO.setEntryParams(params);

        sendErrorEmail(errorDTO.getUserEmail(), errorDTO);
    }

    /**
     * Loads an email template from the classpath.
     *
     * @param templateName the name of the template file
     * @return the content of the template as a string
     * @throws IOException if an I/O error occurs while loading the template
     */
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
