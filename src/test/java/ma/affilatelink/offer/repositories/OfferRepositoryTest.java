
package ma.affilatelink.offer.repositories;

import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.entities.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class OfferRepositoryTest {

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
    private OfferRepository offerRepository;

    @Autowired
    private TagRepository tagRepository;

    private Offer javaOffer;
    private Offer beautyOffer;

    @BeforeEach
    void setUp() {
        offerRepository.deleteAll();
        tagRepository.deleteAll();

        javaOffer = new Offer(
                1L,
                "Java Backend Course",
                "Learn Spring and Java backend development",
                "https://example.com/java",
                "TECH"
        );

        beautyOffer = new Offer(
                2L,
                "Beauty Essentials",
                "Discover skincare products",
                "https://example.com/beauty",
                "BEAUTY"
        );

        Tag programmingTag = tagRepository.saveAndFlush(new Tag("Programming"));
        Tag skincareTag = tagRepository.saveAndFlush(new Tag("Skincare"));

        javaOffer.getTags().add(programmingTag);
        beautyOffer.getTags().add(skincareTag);

        offerRepository.saveAllAndFlush(
                java.util.List.of(javaOffer, beautyOffer)
        );
    }

    @Test
    void shouldFilterOffersByNicheIgnoringCaseAndWhitespace() {
        Page<Offer> result = offerRepository.findAll(
                OfferSpecifications.withFilters(
                        " tech ", null, null, null
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Offer::getTitle)
                .containsExactly("Java Backend Course");
    }

    @Test
    void shouldFilterOffersByAdvertiserId() {
        Page<Offer> result = offerRepository.findAll(
                OfferSpecifications.withFilters(
                        null, 2L, null, null
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Offer::getAdvertiserId)
                .containsExactly(2L);
    }

    @Test
    void shouldFilterOffersByTagIgnoringCaseAndWhitespace() {
        Page<Offer> result = offerRepository.findAll(
                OfferSpecifications.withFilters(
                        null, null, " programming ", null
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Offer::getTitle)
                .containsExactly("Java Backend Course");
    }

    @Test
    void shouldSearchKeywordInOfferTitle() {
        Page<Offer> result = offerRepository.findAll(
                OfferSpecifications.withFilters(
                        null, null, null, "BACKEND"
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Offer::getTitle)
                .containsExactly("Java Backend Course");
    }

    @Test
    void shouldSearchKeywordInOfferDescription() {
        Page<Offer> result = offerRepository.findAll(
                OfferSpecifications.withFilters(
                        null, null, null, "skincare"
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Offer::getTitle)
                .containsExactly("Beauty Essentials");
    }

    @Test
    void shouldCombineMultipleFilters() {
        Page<Offer> result = offerRepository.findAll(
                OfferSpecifications.withFilters(
                        "TECH", 1L, "Programming", "Java"
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Offer::getTitle)
                .containsExactly("Java Backend Course");
    }

    @Test
    void shouldReturnAllOffersWhenNoFiltersAreProvided() {
        Page<Offer> result = offerRepository.findAll(
                OfferSpecifications.withFilters(
                        null, null, null, null
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyPageWhenNoOfferMatchesFilters() {
        Page<Offer> result = offerRepository.findAll(
                OfferSpecifications.withFilters(
                        "UNKNOWN", null, null, null
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result).isEmpty();
    }
}
