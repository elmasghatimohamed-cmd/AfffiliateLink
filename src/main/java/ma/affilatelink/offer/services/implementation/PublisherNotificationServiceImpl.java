package ma.affilatelink.offer.services.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.affilatelink.offer.services.EmailService;
import ma.affilatelink.offer.services.PublisherNotificationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublisherNotificationServiceImpl implements PublisherNotificationService {

    private final EmailService emailService;

    @Override
    public void sendSuspensionNotification(
            String publisherEmail,
            Long advertiserId
    ) {
        String subject = "Important: Advertiser campaign suspension";

        String body = """
                Dear Publisher,

                An advertiser associated with one or more campaigns you may be
                promoting has been suspended.

                Please stop sharing the affected affiliate links on your
                social media accounts.

                Advertiser reference: %d

                Thank you for your cooperation.

                AffiliateLink Team
                """.formatted(advertiserId);

        emailService.sendEmail(publisherEmail, subject, body);

        log.info(
                "Suspension notification sent to publisher for advertiser ID: {}",
                advertiserId
        );
    }

    @Override
    public void prepareSuspensionNotification(Long advertiserId) {
        log.warn(
                "Publisher notification pending recipient lookup for advertiser ID: {}",
                advertiserId
        );
    }
}
