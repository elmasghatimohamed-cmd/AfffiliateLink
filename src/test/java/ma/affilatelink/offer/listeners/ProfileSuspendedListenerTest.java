
package ma.affilatelink.offer.listeners;

import ma.affilatelink.offer.entities.Campaign;
import ma.affilatelink.offer.enums.CampaignStatus;
import ma.affilatelink.offer.repositories.CampaignRepository;
import ma.affilatelink.offer.services.PublisherNotificationService;
import ma.affilatelink.profil.events.ProfileSuspended;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class ProfileSuspendedListenerTest {

    private CampaignRepository campaignRepository;
    private PublisherNotificationService publisherNotificationService;
    private ProfileSuspendedListener listener;

    @BeforeEach
    void setUp() {
        campaignRepository = mock(CampaignRepository.class);
        publisherNotificationService = mock(PublisherNotificationService.class);

        listener = new ProfileSuspendedListener(
                campaignRepository,
                publisherNotificationService
        );
    }

    @Test
    void shouldPauseActiveCampaignsAndNotifyPublishers() {
        Long advertiserId = 1L;

        Campaign firstCampaign = mock(Campaign.class);
        Campaign secondCampaign = mock(Campaign.class);

        when(campaignRepository.findByOffer_AdvertiserIdAndStatus(
                advertiserId,
                CampaignStatus.ACTIVE
        )).thenReturn(List.of(firstCampaign, secondCampaign));

        ProfileSuspended event = new ProfileSuspended(advertiserId);

        listener.on(event);

        verify(campaignRepository).findByOffer_AdvertiserIdAndStatus(
                advertiserId,
                CampaignStatus.ACTIVE
        );

        verify(firstCampaign).setStatus(CampaignStatus.PAUSED);
        verify(secondCampaign).setStatus(CampaignStatus.PAUSED);

        verify(publisherNotificationService)
                .prepareSuspensionNotification(advertiserId);
    }

    @Test
    void shouldNotifyEvenWhenNoActiveCampaignsExist() {
        Long advertiserId = 1L;

        when(campaignRepository.findByOffer_AdvertiserIdAndStatus(
                advertiserId,
                CampaignStatus.ACTIVE
        )).thenReturn(List.of());

        ProfileSuspended event = new ProfileSuspended(advertiserId);

        listener.on(event);

        verify(campaignRepository).findByOffer_AdvertiserIdAndStatus(
                advertiserId,
                CampaignStatus.ACTIVE
        );

        verify(publisherNotificationService)
                .prepareSuspensionNotification(advertiserId);

        verifyNoMoreInteractions(campaignRepository, publisherNotificationService);
    }
}
