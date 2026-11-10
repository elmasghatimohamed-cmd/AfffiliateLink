package ma.affilatelink.offer.services;

public interface PublisherNotificationService {

    void sendSuspensionNotification(
            String publisherEmail,
            Long advertiserId
    );

    void prepareSuspensionNotification(Long advertiserId);

}
