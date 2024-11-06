package com.user_admin.app.service;

import com.user_admin.app.model.dto.ErrorDTO;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
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

    private final EmailAsyncService emailAsyncService;

    public EmailService(EmailAsyncService emailAsyncService) {
        this.emailAsyncService = emailAsyncService;
    }

    /**
     * Sends a password reset email to the specific address.
     *
     * @param to        the recipient's email address
     * @param resetLink the link to reset the password
     */
    public void sendResetPasswordEmail(String to, String resetLink) throws IOException {
        String htmlContent = loadEmailTemplate("password_reset_template.html");
        htmlContent = htmlContent.replace("{{resetLink}}", resetLink);

        // Send email asynchronously
        emailAsyncService.sendEmail(to, "Reset Your Password", htmlContent, "static/password.png");
    }

    /**
     * Sends an account activation email to the specific address.
     *
     * @param to           the recipient's email address
     * @param activateLink the link to activate the account
     */
    public void sendActivateAccountEmail(String to, String activateLink) throws IOException {
        String htmlContent = loadEmailTemplate("activate_account_template.html");
        htmlContent = htmlContent.replace("{{activateLink}}", activateLink);

        // Send email asynchronously
        emailAsyncService.sendEmail(to, "Activate Your Account", htmlContent, "static/user.png");
    }


    /**
     * Sends an error notification email to the specified address.
     *
     * @param to       the recipient's email address
     * @param errorDTO the error information to include in the email
     * @throws IOException if an I/O error occurs while loading the template
     */
    public void sendErrorEmail(String to, ErrorDTO errorDTO) throws IOException {
        String htmlContent = loadEmailTemplate("error_occurred_template.html");
        htmlContent = replaceErrorTemplatePlaceholders(htmlContent, errorDTO);

        // Send email asynchronously
        emailAsyncService.sendEmail(to, "Error occurred", htmlContent, null);
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
     * Replaces placeholders in the provided HTML content with values from the ErrorDTO object.
     * This method is used to dynamically populate an error template with the actual error details.
     *
     * @param htmlContent The HTML content of the error template containing placeholders.
     * @param errorDTO    The ErrorDTO object containing the error details to be inserted into the template.
     * @return The HTML content with placeholders replaced by actual error details.
     */
    private String replaceErrorTemplatePlaceholders(String htmlContent, ErrorDTO errorDTO) {
        // Replace the placeholders in the HTML content with values from the errorDTO object
        htmlContent = htmlContent.replace("{{createdAt}}", errorDTO.getCreatedAt());
        htmlContent = htmlContent.replace("{{user_id}}", errorDTO.getUserEmail());
        htmlContent = htmlContent.replace("{{api}}", errorDTO.getApi());
        htmlContent = htmlContent.replace("{{request}}", errorDTO.getRequest());
        htmlContent = htmlContent.replace("{{message}}", errorDTO.getMessage());
        htmlContent = htmlContent.replace("{{codeLine}}", errorDTO.getCodeLine());

        // Build the entry parameters table dynamically using the values from errorDTO
        StringBuilder entryParamsTable = new StringBuilder();
        for (Map.Entry<String, Object> entry : errorDTO.getEntryParams().entrySet()) {
            entryParamsTable.append("<tr><td>")
                    .append(entry.getKey()) // Add entry parameter key
                    .append("</td><td>")
                    .append(entry.getValue()) // Add entry parameter value
                    .append("</td></tr>");
        }

        // Replace the placeholder for entryParamsTable with the generated table
        htmlContent = htmlContent.replace("{{entryParamsTable}}", entryParamsTable.toString());

        return htmlContent;
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
