package ma.affilatelink.offer.services;

import ma.affilatelink.offer.services.implementation.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailServiceImpl(
                mailSender,
                "noreply@example.com"
        );
    }

    @Test
    void sendsEmailWithExpectedContent() {
        String recipient = "publisher@example.com";
        String subject = "Campaign suspension";
        String body = "Please stop sharing the affected affiliate links.";

        emailService.sendEmail(recipient, subject, body);

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals("noreply@example.com", capturedMessage.getFrom());
        assertEquals(recipient, capturedMessage.getTo()[0]);
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(body, capturedMessage.getText());
    }
}