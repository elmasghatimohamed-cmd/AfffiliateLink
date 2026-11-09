package ma.affilatelink.offer.services;

import ma.affilatelink.common.exceptions.InvalidCampaignStatusException;
import ma.affilatelink.offer.dto.CampaignCreateRequest;
import ma.affilatelink.offer.dto.CampaignMapper;
import ma.affilatelink.offer.entities.Campaign;
import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.enums.CampaignStatus;
import ma.affilatelink.offer.enums.PayoutModel;
import ma.affilatelink.offer.repositories.CampaignRepository;
import ma.affilatelink.offer.repositories.OfferRepository;
import ma.affilatelink.profil.api.ProfileApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    private static final Long OFFER_ID = 1L;
    private static final Long CAMPAIGN_ID = 10L;
    private static final Long ADVERTISER_ID = 55L;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private CampaignMapper campaignMapper;

    @Mock
    private ProfileApi profileApi;

    @InjectMocks
    private CampaignService campaignService;

    private Offer offer;

    @BeforeEach
    void setUp() {
        offer = mock(Offer.class);
    }

    @Test
    void shouldActivateDraftCampaign() {
        Campaign campaign = mockCampaign(CampaignStatus.DRAFT);

        mockCampaignActivation(campaign, LocalDate.now().plusDays(5));

        when(campaignRepository.findByOffer_IdAndStatus(
                OFFER_ID,
                CampaignStatus.ACTIVE
        )).thenReturn(Optional.empty());

        campaignService.updateCampaignStatus(
                CAMPAIGN_ID,
                CampaignStatus.ACTIVE
        );

        verify(campaign).setStatus(CampaignStatus.ACTIVE);
    }

    @Test
    void shouldReactivatePausedCampaign() {
        Campaign campaign = mockCampaign(CampaignStatus.PAUSED);

        mockCampaignActivation(campaign, LocalDate.now().plusDays(5));

        when(campaignRepository.findByOffer_IdAndStatus(
                OFFER_ID,
                CampaignStatus.ACTIVE
        )).thenReturn(Optional.empty());

        campaignService.updateCampaignStatus(
                CAMPAIGN_ID,
                CampaignStatus.ACTIVE
        );

        verify(campaign).setStatus(CampaignStatus.ACTIVE);
    }

    @Test
    void shouldPauseActiveCampaign() {
        Campaign campaign = mockCampaign(CampaignStatus.ACTIVE);

        mockCampaignLookup(campaign);

        campaignService.updateCampaignStatus(
                CAMPAIGN_ID,
                CampaignStatus.PAUSED
        );

        verify(campaign).setStatus(CampaignStatus.PAUSED);

        verify(offerRepository, never())
                .findByCampaignIdForUpdate(any());
    }

    @Test
    void shouldEndActiveCampaign() {
        Campaign campaign = mockCampaign(CampaignStatus.ACTIVE);

        mockCampaignLookup(campaign);

        campaignService.updateCampaignStatus(
                CAMPAIGN_ID,
                CampaignStatus.ENDED
        );

        verify(campaign).setStatus(CampaignStatus.ENDED);
    }

    @Test
    void shouldEndPausedCampaign() {
        Campaign campaign = mockCampaign(CampaignStatus.PAUSED);

        mockCampaignLookup(campaign);

        campaignService.updateCampaignStatus(
                CAMPAIGN_ID,
                CampaignStatus.ENDED
        );

        verify(campaign).setStatus(CampaignStatus.ENDED);
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        Campaign campaign = mockCampaign(CampaignStatus.DRAFT);

        mockCampaignLookup(campaign);

        assertThrows(
                InvalidCampaignStatusException.class,
                () -> campaignService.updateCampaignStatus(
                        CAMPAIGN_ID,
                        CampaignStatus.PAUSED
                )
        );

        verify(campaign, never()).setStatus(any());
    }

    @Test
    void shouldRejectAnyTransitionFromEndedCampaign() {
        Campaign campaign = mockCampaign(CampaignStatus.ENDED);

        mockCampaignActivationLock(campaign);

        assertThrows(
                InvalidCampaignStatusException.class,
                () -> campaignService.updateCampaignStatus(
                        CAMPAIGN_ID,
                        CampaignStatus.ACTIVE
                )
        );

        verify(campaign, never()).setStatus(any());
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> campaignService.updateCampaignStatus(
                        CAMPAIGN_ID,
                        null
                )
        );

        verifyNoInteractions(campaignRepository, offerRepository);
    }

    @Test
    void shouldRejectActivationOfExpiredCampaign() {
        Campaign campaign = mockCampaign(CampaignStatus.DRAFT);

        mockCampaignActivationLock(campaign);

        when(campaign.getEndDate())
                .thenReturn(LocalDate.now().minusDays(1));

        assertThrows(
                InvalidCampaignStatusException.class,
                () -> campaignService.updateCampaignStatus(
                        CAMPAIGN_ID,
                        CampaignStatus.ACTIVE
                )
        );

        verify(campaign, never()).setStatus(any());

        verify(campaignRepository, never())
                .findByOffer_IdAndStatus(any(), any());
    }

    @Test
    void shouldAllowActivationWhenCampaignEndsToday() {
        Campaign campaign = mockCampaign(CampaignStatus.DRAFT);

        mockCampaignActivation(campaign, LocalDate.now());

        when(campaignRepository.findByOffer_IdAndStatus(
                OFFER_ID,
                CampaignStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> campaignService.updateCampaignStatus(
                        CAMPAIGN_ID,
                        CampaignStatus.ACTIVE
                )
        );

        verify(campaign).setStatus(CampaignStatus.ACTIVE);
    }

    @Test
    void shouldPausePreviousActiveCampaignWhenActivatingAnother() {
        Campaign campaign = mockCampaign(CampaignStatus.DRAFT);

        when(campaign.getId()).thenReturn(CAMPAIGN_ID);

        mockCampaignActivation(campaign, LocalDate.now().plusDays(5));

        Campaign previousActiveCampaign = mock(Campaign.class);

        when(previousActiveCampaign.getId()).thenReturn(20L);

        when(campaignRepository.findByOffer_IdAndStatus(
                OFFER_ID,
                CampaignStatus.ACTIVE
        )).thenReturn(Optional.of(previousActiveCampaign));

        campaignService.updateCampaignStatus(
                CAMPAIGN_ID,
                CampaignStatus.ACTIVE
        );

        verify(previousActiveCampaign)
                .setStatus(CampaignStatus.PAUSED);

        verify(campaign).setStatus(CampaignStatus.ACTIVE);
    }

    @Test
    void shouldNotPauseSameCampaignWhenAlreadyActive() {
        Campaign campaign = mockCampaign(CampaignStatus.PAUSED);

        when(campaign.getId()).thenReturn(CAMPAIGN_ID);

        mockCampaignActivation(campaign, LocalDate.now().plusDays(5));

        when(campaignRepository.findByOffer_IdAndStatus(
                OFFER_ID,
                CampaignStatus.ACTIVE
        )).thenReturn(Optional.of(campaign));

        campaignService.updateCampaignStatus(
                CAMPAIGN_ID,
                CampaignStatus.ACTIVE
        );

        verify(campaign).setStatus(CampaignStatus.ACTIVE);
        verify(campaign, never()).setStatus(CampaignStatus.PAUSED);
    }

    @Test
    void shouldThrowWhenCampaignDoesNotExistDuringActivationLock() {
        when(offerRepository.findByCampaignIdForUpdate(CAMPAIGN_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> campaignService.updateCampaignStatus(
                        CAMPAIGN_ID,
                        CampaignStatus.ACTIVE
                )
        );

        verify(campaignRepository, never()).findById(any());
    }

    @Test
    void shouldRejectNullCampaignRequest() {
        mockActiveAdvertiser();

        assertInvalidRequest(null);
    }

    @Test
    void shouldRejectNullCampaignName() {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                null,
                validStartDate(),
                validEndDate(),
                BigDecimal.valueOf(100),
                PayoutModel.CPC,
                BigDecimal.valueOf(5)
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void shouldRejectBlankCampaignName(String name) {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                name,
                validStartDate(),
                validEndDate(),
                BigDecimal.valueOf(100),
                PayoutModel.CPC,
                BigDecimal.valueOf(5)
        ));
    }

    @Test
    void shouldRejectMissingStartDate() {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                "Campaign",
                null,
                validEndDate(),
                BigDecimal.valueOf(100),
                PayoutModel.CPC,
                BigDecimal.valueOf(5)
        ));
    }

    @Test
    void shouldRejectMissingEndDate() {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                "Campaign",
                validStartDate(),
                null,
                BigDecimal.valueOf(100),
                PayoutModel.CPC,
                BigDecimal.valueOf(5)
        ));
    }

    @Test
    void shouldRejectEndDateEqualToStartDate() {
        mockActiveAdvertiser();

        LocalDate startDate = validStartDate();

        assertInvalidRequest(request(
                "Campaign",
                startDate,
                startDate,
                BigDecimal.valueOf(100),
                PayoutModel.CPC,
                BigDecimal.valueOf(5)
        ));
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                "Campaign",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(2),
                BigDecimal.valueOf(100),
                PayoutModel.CPC,
                BigDecimal.valueOf(5)
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void shouldRejectZeroOrNegativeBudget(String budget) {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                "Campaign",
                validStartDate(),
                validEndDate(),
                new BigDecimal(budget),
                PayoutModel.CPC,
                BigDecimal.valueOf(5)
        ));
    }

    @Test
    void shouldRejectNullBudget() {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                "Campaign",
                validStartDate(),
                validEndDate(),
                null,
                PayoutModel.CPC,
                BigDecimal.valueOf(5)
        ));
    }

    @Test
    void shouldRejectMissingPayoutModel() {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                "Campaign",
                validStartDate(),
                validEndDate(),
                BigDecimal.valueOf(100),
                null,
                BigDecimal.valueOf(5)
        ));
    }

    @Test
    void shouldRejectMissingPayoutValue() {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                "Campaign",
                validStartDate(),
                validEndDate(),
                BigDecimal.valueOf(100),
                PayoutModel.CPC,
                null
        ));
    }

    @ParameterizedTest
    @MethodSource("fixedPayoutModels")
    void shouldRejectNonPositiveFixedPayoutValues(PayoutModel payoutModel) {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                "Campaign",
                validStartDate(),
                validEndDate(),
                BigDecimal.valueOf(100),
                payoutModel,
                BigDecimal.ZERO
        ));

        assertInvalidRequest(request(
                "Campaign",
                validStartDate(),
                validEndDate(),
                BigDecimal.valueOf(100),
                payoutModel,
                BigDecimal.valueOf(-1)
        ));
    }

    static Stream<PayoutModel> fixedPayoutModels() {
        return Stream.of(
                PayoutModel.CPC,
                PayoutModel.CPL,
                PayoutModel.CPA_FIXED
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "100.01", "101"})
    void shouldRejectPercentagePayoutOutsideAllowedRange(String value) {
        mockActiveAdvertiser();

        assertInvalidRequest(request(
                "Campaign",
                validStartDate(),
                validEndDate(),
                BigDecimal.valueOf(100),
                PayoutModel.CPA_PERCENT,
                new BigDecimal(value)
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "100"})
    void shouldAcceptPercentagePayoutBoundaries(String value) {
        mockActiveAdvertiser();

        when(campaignRepository.save(any(Campaign.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> campaignService.createCampaign(
                OFFER_ID,
                request(
                        "Campaign",
                        validStartDate(),
                        validEndDate(),
                        BigDecimal.valueOf(100),
                        PayoutModel.CPA_PERCENT,
                        new BigDecimal(value)
                )
        ));

        verify(campaignRepository).save(any(Campaign.class));
    }

    private Campaign mockCampaign(CampaignStatus status) {
        Campaign campaign = mock(Campaign.class);

        when(campaign.getStatus()).thenReturn(status);

        return campaign;
    }

    private void mockCampaignLookup(Campaign campaign) {
        when(campaignRepository.findById(CAMPAIGN_ID))
                .thenReturn(Optional.of(campaign));
    }

    /**
     * Mocks only the operations performed before the campaign
     * status transition is validated.
     */
    private void mockCampaignActivationLock(Campaign campaign) {
        when(offerRepository.findByCampaignIdForUpdate(CAMPAIGN_ID))
                .thenReturn(Optional.of(offer));

        mockCampaignLookup(campaign);
    }

    /**
     * Adds the dependencies required after a valid activation
     * transition has been established.
     */
    private void mockCampaignActivation(
            Campaign campaign,
            LocalDate endDate
    ) {
        mockCampaignActivationLock(campaign);

        when(campaign.getEndDate()).thenReturn(endDate);
        when(campaign.getOffer()).thenReturn(offer);
        when(offer.getId()).thenReturn(OFFER_ID);
    }

    private void mockActiveAdvertiser() {
        when(offer.getAdvertiserId()).thenReturn(ADVERTISER_ID);

        when(offerRepository.findById(OFFER_ID))
                .thenReturn(Optional.of(offer));

        when(profileApi.isAdvertiserActive(ADVERTISER_ID))
                .thenReturn(true);
    }

    private void assertInvalidRequest(CampaignCreateRequest request) {
        assertThrows(
                IllegalArgumentException.class,
                () -> campaignService.createCampaign(OFFER_ID, request)
        );

        verify(campaignRepository, never()).save(any());
    }

    private CampaignCreateRequest request(
            String name,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal budget,
            PayoutModel payoutModel,
            BigDecimal payoutValue
    ) {
        return new CampaignCreateRequest(
                name,
                startDate,
                endDate,
                budget,
                payoutModel,
                payoutValue
        );
    }

    private LocalDate validStartDate() {
        return LocalDate.now().plusDays(1);
    }

    private LocalDate validEndDate() {
        return LocalDate.now().plusDays(10);
    }
}
