package ma.affilatelink.profil.repositories;

import ma.affilatelink.profil.entities.Advertiser;
import ma.affilatelink.profil.enums.ProfileStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class AdvertiserRepositoryTest {

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
    private AdvertiserRepository advertiserRepository;

    @Test
    void shouldReturnTrueWhenAdvertiserExistsWithRequestedStatus() {
        Advertiser advertiser = new Advertiser(
                "First Company",
                "Retail",
                "https://first-company.com",
                "company@test.com"
        );

        advertiser.activate();
        Advertiser savedAdvertiser = advertiserRepository.saveAndFlush(advertiser);

        boolean exists = advertiserRepository.existsByIdAndStatus(
                savedAdvertiser.getId(),
                ProfileStatus.ACTIVE
        );

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAdvertiserHasDifferentStatus() {
        Advertiser advertiser = new Advertiser(
                "Second Company",
                "Technology",
                "https://second-company.com",
                "second@test.com"
        );

        Advertiser savedAdvertiser = advertiserRepository.saveAndFlush(advertiser);

        boolean exists = advertiserRepository.existsByIdAndStatus(
                savedAdvertiser.getId(),
                ProfileStatus.ACTIVE
        );

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAdvertiserDoesNotExist() {
        Long nonExistingId = 999L;

        boolean exists = advertiserRepository.existsByIdAndStatus(
                nonExistingId,
                ProfileStatus.ACTIVE
        );

        assertThat(exists).isFalse();
    }
}
