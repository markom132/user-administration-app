package com.user_admin.app.services;

import com.user_admin.app.model.dto.ErrorDTO;
import com.user_admin.app.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(mailSender);
    }

    @Test
    void sendResetPasswordEmail_Success() throws Exception {
        String to = "recipient@example.com";
        String resetLink = "http://example.com/reset-password";

        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendResetPasswordEmail(to, resetLink);

        verify(mailSender).send(message);
    }

    @Test
    void sendResetPasswordEmail_MessagingException() throws Exception {
        String to = "recipient@example.com";
        String resetLink = "http://example.com/reset-password";

        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        doThrow(new RuntimeException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendResetPasswordEmail(to, resetLink);
        });
        assertTrue(exception.getMessage().contains("Failed to send email"));
    }

    @Test
    void sendActivateAccountEmail_Success() throws Exception {
        String to = "recipient@example.com";
        String activateLink = "http://example.com/activate-account";

        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendActivateAccountEmail(to, activateLink);

        verify(mailSender).send(message);
    }

    @Test
    void sendActivateAccountEmail_MessagingException() throws Exception {
        String to = "recipient@example.com";
        String activateLink = "http://example.com/activate-account";

        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        doThrow(new RuntimeException("Failed to send reset password email")).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendActivateAccountEmail(to, activateLink);
        });
        assertTrue(exception.getMessage().contains("Failed to send reset password email"));
    }

    @Test
    void sendErrorEmail_Success() throws Exception {
        String to = "recipient@example.com";
        ErrorDTO errorDTO = createErrorDTO();

        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendErrorEmail(to, errorDTO);

        verify(mailSender).send(message);
    }

    @Test
    void sendErrorEmail_MessagingException() throws Exception {
        String to = "recipient@example.com";
        ErrorDTO errorDTO = createErrorDTO();

        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        doThrow(new MailException("Failed to send error occurred email") {
        }).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendErrorEmail(to, errorDTO);
        });
        assertTrue(exception.getMessage().contains("Failed to send error occurred email"));
    }


    private ErrorDTO createErrorDTO() {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCreatedAt("2024-11-04T10:00:00Z");
        errorDTO.setUserEmail("user@example.com");
        errorDTO.setApi("/api/test");
        errorDTO.setRequest("GET /api/test");
        errorDTO.setMessage("An error occurred");
        errorDTO.setCodeLine(String.valueOf(42));

        Map<String, Object> entryParams = new HashMap<>();
        entryParams.put("param1", "value1");
        entryParams.put("param2", "value2");
        errorDTO.setEntryParams(entryParams);

        return errorDTO;
    }

}
