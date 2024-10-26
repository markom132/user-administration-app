package com.user_admin.app.service;

import com.user_admin.app.model.dto.ErrorDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
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
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send error occurred email", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
