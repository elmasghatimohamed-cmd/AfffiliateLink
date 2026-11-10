package ma.affilatelink.offer.services;

import ma.affilatelink.offer.services.implementation.PublisherNotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PublisherNotificationServiceTest {

    private EmailService emailService;
    private PublisherNotificationService notificationService;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        notificationService = new PublisherNotificationServiceImpl(emailService);
    }

    @Test
    void sendsSuspensionNotificationToPublisher() {
        String publisherEmail = "publisher@example.com";
        Long advertiserId = 19L;

        notificationService.sendSuspensionNotification(
                publisherEmail,
                advertiserId
        );

        ArgumentCaptor<String> recipientCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> subjectCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> bodyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailService).sendEmail(
                recipientCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture()
        );

        assertEquals(publisherEmail, recipientCaptor.getValue());

        assertEquals(
                "Important: Advertiser campaign suspension",
                subjectCaptor.getValue()
        );

        assertTrue(
                bodyCaptor.getValue().contains(
                        "stop sharing the affected affiliate links"
                )
        );

        assertTrue(bodyCaptor.getValue().contains("19"));
    }
}