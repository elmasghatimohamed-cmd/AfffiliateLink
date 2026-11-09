package ma.affilatelink.profil.repositories;

import ma.affilatelink.profil.entities.Advertiser;
import ma.affilatelink.profil.entities.Publisher;
import ma.affilatelink.profil.enums.Niche;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class ProfileRepositoryTest {

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
    private ProfileRepository profileRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Test
    void shouldFindEmailWhenProfileIsPublisher() {
        Publisher publisher = new Publisher(
                "John",
                "Doe",
                Niche.BEAUTY,
                "Beauty content creator",
                "Morocco",
                "john@test.com"
        );

        publisherRepository.saveAndFlush(publisher);

        boolean exists = profileRepository.existsByEmail("john@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldFindEmailWhenProfileIsAdvertiser() {
        Advertiser advertiser = new Advertiser(
                "First Company",
                "Retail",
                "https://first-company.com",
                "company@test.com"
        );

        advertiserRepository.saveAndFlush(advertiser);

        boolean exists = profileRepository.existsByEmail("company@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        String email = "unknown@test.com";

        boolean exists = profileRepository.existsByEmail(email);

        assertThat(exists).isFalse();
    }
}
