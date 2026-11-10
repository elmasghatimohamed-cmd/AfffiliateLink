package ma.affilatelink.profil.integration;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
import ma.affilatelink.common.exceptions.EmailAlreadyExistsException;
import ma.affilatelink.profil.dto.AdvertiserRequest;
import ma.affilatelink.profil.entities.Advertiser;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.repositories.AdvertiserRepository;
import ma.affilatelink.profil.repositories.ProfileRepository;
import ma.affilatelink.offer.services.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "MAIL_HOST=localhost",
        "MAIL_USERNAME=test",
        "MAIL_PASSWORD=test"
})
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdvertiserRegistrationIntegrationTest {

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
    private ProfileRepository profileRepository;

    @MockitoBean
    private EmailService emailService;

    @Test
    void shouldRegisterAdvertiserAndPersistIt() throws Exception {
        AdvertiserRequest request = new AdvertiserRequest(
                "integration-advertiser@test.com",
                "Integration Company",
                "Technology",
                "https://integration-company.com"
        );

        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email")
                        .value(request.email()))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"))
                .andExpect(jsonPath("$.companyName")
                        .value(request.companyName()))
                .andExpect(jsonPath("$.sector")
                        .value(request.sector()))
                .andExpect(jsonPath("$.website")
                        .value(request.website()));

        Advertiser advertiser = advertiserRepository.findAll()
                .stream()
                .filter(item -> item.getEmail().equals(request.email()))
                .findFirst()
                .orElseThrow();

        assertThat(advertiser.getStatus())
                .isEqualTo(ProfileStatus.PENDING);

        assertThat(advertiser.getCreatedAt())
                .isNotNull();

        assertThat(profileRepository.existsByEmail(request.email()))
                .isTrue();
    }

    @Test
    void shouldRejectDuplicateEmailAndPreserveExistingAdvertiser()
            throws Exception {

        AdvertiserRequest request = new AdvertiserRequest(
                "duplicate-advertiser@test.com",
                "Original Company",
                "Technology",
                "https://original-company.com"
        );

        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        long advertiserCountBefore = advertiserRepository.count();
        long profileCountBefore = profileRepository.count();

        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        assertThat(advertiserRepository.count())
                .isEqualTo(advertiserCountBefore);

        assertThat(profileRepository.count())
                .isEqualTo(profileCountBefore);

        Advertiser existingAdvertiser = advertiserRepository.findAll()
                .stream()
                .filter(item -> item.getEmail().equals(request.email()))
                .findFirst()
                .orElseThrow();

        assertThat(existingAdvertiser.getCompanyName())
                .isEqualTo("Original Company");
    }
}
