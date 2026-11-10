package ma.affilatelink.offer.integration;

import ma.affilatelink.offer.dto.CampaignCreateRequest;
import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.UpdateCampaignStatusRequest;
import ma.affilatelink.offer.entities.Campaign;
import ma.affilatelink.offer.enums.CampaignStatus;
import ma.affilatelink.offer.enums.PayoutModel;
import ma.affilatelink.offer.repositories.CampaignRepository;
import ma.affilatelink.offer.services.EmailService;
import ma.affilatelink.profil.entities.Advertiser;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.repositories.AdvertiserRepository;
import ma.affilatelink.profil.services.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "MAIL_HOST=localhost",
        "MAIL_USERNAME=test",
        "MAIL_PASSWORD=test"
})
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OfferCampaignLifecycleIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private CampaignRepository campaignRepository;

    @MockitoBean
    private EmailService emailService;

    @Test
    void shouldPausePreviousActiveCampaignWhenAnotherCampaignIsActivated()
            throws Exception {

        // Arrange: create and activate an advertiser.
        String uniqueId = UUID.randomUUID().toString();

        Advertiser advertiser = advertiserRepository.saveAndFlush(
                new Advertiser(
                        "Lifecycle Test Company",
                        "TECH",
                        "https://example.com",
                        "lifecycle-" + uniqueId + "@example.com"
                )
        );

        Long advertiserId = advertiser.getId();

        adminService.changeStatus(advertiserId, ProfileStatus.ACTIVE);

        // Create an offer through the real HTTP endpoint.
        CreateOfferRequest offerRequest = new CreateOfferRequest(
                advertiserId,
                "Lifecycle Integration Offer",
                "Offer used to verify campaign lifecycle behavior",
                "https://example.com/offer",
                "TECH",
                Set.of("lifecycle-" + uniqueId)
        );

        String offerResponse = mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long offerId = objectMapper.readTree(offerResponse)
                .get("id")
                .asLong();

        // Create two campaigns through the real HTTP endpoint.
        CampaignCreateRequest firstCampaignRequest = createCampaignRequest(
                "First Lifecycle Campaign"
        );

        CampaignCreateRequest secondCampaignRequest = createCampaignRequest(
                "Second Lifecycle Campaign"
        );

        Long firstCampaignId = createCampaign(
                offerId,
                firstCampaignRequest
        );

        Long secondCampaignId = createCampaign(
                offerId,
                secondCampaignRequest
        );

        // Act: activate the first campaign.
        updateCampaignStatus(firstCampaignId, CampaignStatus.ACTIVE);

        assertThat(findCampaign(firstCampaignId).getStatus())
                .isEqualTo(CampaignStatus.ACTIVE);

        // Act: activate the second campaign.
        updateCampaignStatus(secondCampaignId, CampaignStatus.ACTIVE);

        // Assert: the previous campaign is automatically paused.
        assertThat(findCampaign(firstCampaignId).getStatus())
                .isEqualTo(CampaignStatus.PAUSED);

        assertThat(findCampaign(secondCampaignId).getStatus())
                .isEqualTo(CampaignStatus.ACTIVE);
    }

    private CampaignCreateRequest createCampaignRequest(String name) {
        return new CampaignCreateRequest(
                name,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                new BigDecimal("1000.00"),
                PayoutModel.CPC,
                new BigDecimal("2.50")
        );
    }

    private Long createCampaign(
            Long offerId,
            CampaignCreateRequest request
    ) throws Exception {

        String response = mockMvc.perform(
                        post("/api/offres/{id}/campaigns", offerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private void updateCampaignStatus(
            Long campaignId,
            CampaignStatus status
    ) throws Exception {

        UpdateCampaignStatusRequest request =
                new UpdateCampaignStatusRequest(status);

        mockMvc.perform(
                        patch("/api/campaigns/{id}/status", campaignId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNoContent());
    }

    private Campaign findCampaign(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow();
    }
}
