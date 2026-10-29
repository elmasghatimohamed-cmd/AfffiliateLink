package ma.affilatelink.offer.dto;

import ma.affilatelink.offer.entities.Offer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OfferMapperTest {

    private OfferMapper offerMapper;

    @BeforeEach
    void setUp() {
        offerMapper = Mappers.getMapper(OfferMapper.class);
    }

    @Test
    void shouldMapCreateRequestToOffer() {
        CreateOfferRequest request = new CreateOfferRequest(
                1L,
                "Java Course",
                "Learn Java and Spring Boot",
                "https://example.com/java",
                "TECH",
                Set.of("Java", "Spring")
        );

        Offer offer = offerMapper.toEntity(request);

        assertNotNull(offer);
        assertEquals(1L, offer.getAdvertiserId());
        assertEquals("Java Course", offer.getTitle());
        assertEquals("Learn Java and Spring Boot", offer.getDescription());
        assertEquals("https://example.com/java", offer.getLandingUrl());
        assertEquals("TECH", offer.getNiche());
        assertNotNull(offer.getCreatedAt());
        assertTrue(offer.getTags().isEmpty());
    }

    @Test
    void shouldUpdateOfferDetails() {
        Offer offer = new Offer(
                1L,
                "Old title",
                "Old description",
                "https://example.com/old",
                "TECH"
        );

        UpdateOfferRequest request = new UpdateOfferRequest(
                "New title",
                "New description",
                "https://example.com/new",
                "EDUCATION",
                Set.of("Java")
        );

        offerMapper.updateEntity(request, offer);

        assertEquals("New title", offer.getTitle());
        assertEquals("New description", offer.getDescription());
        assertEquals("https://example.com/new", offer.getLandingUrl());
        assertEquals("EDUCATION", offer.getNiche());


        assertEquals(1L, offer.getAdvertiserId());
        assertTrue(offer.getTags().isEmpty());
    }

    @Test
    void shouldMapOfferToResponse() {
        Offer offer = new Offer(
                1L,
                "Java Course",
                "Learn Java and Spring Boot",
                "https://example.com/java",
                "TECH"
        );

        OfferResponse response = offerMapper.toResponse(offer);

        assertNotNull(response);
        assertEquals(1L, response.advertiserId());
        assertEquals("Java Course", response.title());
        assertEquals("Learn Java and Spring Boot", response.description());
        assertEquals("https://example.com/java", response.landingUrl());
        assertEquals("TECH", response.niche());
        assertNotNull(response.createdAt());
        assertTrue(response.tags().isEmpty());
    }
}