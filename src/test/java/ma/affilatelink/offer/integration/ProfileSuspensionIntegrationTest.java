package ma.affilatelink.offer.integration;

import ma.affilatelink.offer.entities.Campaign;
import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.enums.CampaignStatus;
import ma.affilatelink.offer.enums.PayoutModel;
import ma.affilatelink.offer.repositories.CampaignRepository;
import ma.affilatelink.offer.repositories.OfferRepository;
import ma.affilatelink.offer.services.EmailService;
import ma.affilatelink.profil.entities.Advertiser;
import ma.affilatelink.profil.entities.Profile;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.repositories.AdvertiserRepository;
import ma.affilatelink.profil.repositories.ProfileRepository;
import ma.affilatelink.profil.services.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(properties = {
        "MAIL_HOST=localhost",
        "MAIL_USERNAME=test",
        "MAIL_PASSWORD=test"
})
@Testcontainers
@EnableScenarios
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProfileSuspensionIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private AdminService adminService;

    @Test
    void shouldPauseAdvertiserCampaignWhenProfileIsSuspended(
            Scenario scenario
    ) {
        Advertiser advertiser = advertiserRepository.saveAndFlush(
                new Advertiser(
                        "Integration Test Company",
                        "TECH",
                        "https://example.com",
                        "integration-test@example.com"
                )
        );

        Long advertiserId = advertiser.getId();

        adminService.changeStatus(advertiserId, ProfileStatus.ACTIVE);

        Offer offer = offerRepository.saveAndFlush(
                new Offer(
                        advertiserId,
                        "Integration Test Offer",
                        "Offer used to verify suspension event handling",
                        "https://example.com/offer",
                        "TECH"
                )
        );

        Campaign campaign = campaignRepository.saveAndFlush(
                new Campaign(
                        "Integration Test Campaign",
                        LocalDate.now(),
                        LocalDate.now().plusDays(30),
                        new BigDecimal("1000.00"),
                        PayoutModel.CPC,
                        new BigDecimal("2.50"),
                        CampaignStatus.ACTIVE,
                        offer
                )
        );

        Long campaignId = campaign.getId();

        scenario.stimulate(() ->
                        adminService.changeStatus(
                                advertiserId,
                                ProfileStatus.SUSPENDED
                        )
                )
                .andWaitForStateChange(
                        () -> new SuspensionState(
                                profileRepository.findById(advertiserId)
                                        .map(Profile::getStatus)
                                        .orElse(null),
                                campaignRepository.findById(campaignId)
                                        .map(Campaign::getStatus)
                                        .orElse(null)
                        ),
                        state ->
                                state.profileStatus() == ProfileStatus.SUSPENDED
                                        && state.campaignStatus() == CampaignStatus.PAUSED
                )
                .andVerify(state -> {
                    assertThat(state.profileStatus())
                            .isEqualTo(ProfileStatus.SUSPENDED);

                    assertThat(state.campaignStatus())
                            .isEqualTo(CampaignStatus.PAUSED);
                });
    }

    private record SuspensionState(
            ProfileStatus profileStatus,
            CampaignStatus campaignStatus
    ) {}
}
