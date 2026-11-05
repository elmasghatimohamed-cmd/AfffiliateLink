package ma.affilatelink.offer.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.affilatelink.offer.enums.CampaignStatus;
import ma.affilatelink.offer.repositories.CampaignRepository;
import ma.affilatelink.offer.services.PublisherNotificationService;
import ma.affilatelink.profil.events.ProfileSuspended;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileSuspendedListener {

    private final CampaignRepository campaignRepository;
    private final PublisherNotificationService publisherNotificationService;

    @ApplicationModuleListener
    public void on(ProfileSuspended event) {
        Long advertiserId = event.profileId();

        log.info(
                "Processing suspension event for advertiser ID: {}",
                advertiserId
        );

        var activeCampaigns =
                campaignRepository.findByOffer_AdvertiserIdAndStatus(
                        advertiserId,
                        CampaignStatus.ACTIVE
                );

        log.info(
                "Found {} active campaigns for suspended advertiser ID: {}",
                activeCampaigns.size(),
                advertiserId
        );

        activeCampaigns.forEach(
                campaign -> campaign.setStatus(CampaignStatus.PAUSED)
        );

        log.info(
                "Paused {} campaigns for advertiser ID: {}",
                activeCampaigns.size(),
                advertiserId
        );

        publisherNotificationService.prepareSuspensionNotification(
                advertiserId
        );

        log.info(
                "Suspension event processing completed for advertiser ID: {}",
                advertiserId
        );
    }
}