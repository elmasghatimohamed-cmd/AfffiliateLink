package ma.affilatelink.offer.integration;

import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.entities.Tag;
import ma.affilatelink.offer.repositories.OfferRepository;
import ma.affilatelink.offer.repositories.OfferSpecifications;
import ma.affilatelink.offer.repositories.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class OfferDiscoveryIntegrationTest {

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

    @BeforeEach
    void setUp() {
        createOffer(
                1L,
                "Java Backend Developer",
                "Build backend applications with Spring",
                "TECH",
                "Java",
                "Backend"
        );

        createOffer(
                1L,
                "Spring Boot Essentials",
                "Learn modern Java development",
                "TECH",
                "Java",
                "Spring"
        );

        createOffer(
                2L,
                "Beauty Essentials",
                "Skincare products for everyday use",
                "BEAUTY",
                "Skincare",
                "Lifestyle"
        );

        createOffer(
                2L,
                "Advanced Java",
                "Enterprise software development",
                "TECH",
                "Java",
                "Backend"
        );
    }

    @Test
    void shouldFilterOffersByNicheIgnoringCase() {
        Page<Offer> result = search("tech", null, null, null);

        assertThat(result.getContent())
                .hasSize(3)
                .allMatch(offer -> offer.getNiche().equals("TECH"));
    }

    @Test
    void shouldFilterOffersByAdvertiserId() {
        Page<Offer> result = search(null, 1L, null, null);

        assertThat(result.getContent())
                .hasSize(2)
                .allMatch(offer -> offer.getAdvertiserId().equals(1L));
    }

    @Test
    void shouldFilterOffersByTagIgnoringCase() {
        Page<Offer> result = search(null, null, "java", null);

        assertThat(result.getContent())
                .hasSize(3)
                .allMatch(offer -> offer.getTags().stream()
                        .anyMatch(tag -> tag.getName().equals("Java")));
    }

    @Test
    void shouldMatchKeywordInTitleOrDescriptionIgnoringCase() {
        Page<Offer> result = search(null, null, null, "BACKEND");

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().get(0).getTitle())
                .isEqualTo("Java Backend Developer");
    }

    @Test
    void shouldCombineAllProvidedFiltersUsingAnd() {
        Page<Offer> result = search(
                "TECH",
                1L,
                "Java",
                "backend"
        );

        assertThat(result.getContent())
                .hasSize(1);

        Offer matchingOffer = result.getContent().get(0);

        assertThat(matchingOffer.getTitle())
                .isEqualTo("Java Backend Developer");
    }

    @Test
    void shouldApplyPaginationAndSortingToFilteredResults() {
        Page<Offer> firstPage = offerRepository.findAll(
                OfferSpecifications.withFilters("TECH", null, null, null),
                PageRequest.of(
                        0,
                        2,
                        Sort.by(Sort.Direction.ASC, "title")
                )
        );

        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent())
                .extracting(Offer::getTitle)
                .containsExactly(
                        "Advanced Java",
                        "Java Backend Developer"
                );

        Page<Offer> secondPage = offerRepository.findAll(
                OfferSpecifications.withFilters("TECH", null, null, null),
                PageRequest.of(
                        1,
                        2,
                        Sort.by(Sort.Direction.ASC, "title")
                )
        );

        assertThat(secondPage.getContent())
                .extracting(Offer::getTitle)
                .containsExactly("Spring Boot Essentials");
    }

    private Page<Offer> search(
            String niche,
            Long advertiserId,
            String tag,
            String keyword
    ) {
        return offerRepository.findAll(
                OfferSpecifications.withFilters(
                        niche,
                        advertiserId,
                        tag,
                        keyword
                ),
                PageRequest.of(0, 20)
        );
    }

    private void createOffer(
            Long advertiserId,
            String title,
            String description,
            String niche,
            String... tagNames
    ) {
        Offer offer = new Offer(
                advertiserId,
                title,
                description,
                "https://example.com/" + title.toLowerCase()
                        .replace(" ", "-"),
                niche
        );

        Set<Tag> tags = java.util.Arrays.stream(tagNames)
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toSet());

        offer.getTags().addAll(tags);
        offerRepository.saveAndFlush(offer);
    }
}
