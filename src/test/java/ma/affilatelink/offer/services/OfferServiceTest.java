package ma.affilatelink.offer.services;

import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.OfferMapper;
import ma.affilatelink.offer.dto.OfferResponse;
import ma.affilatelink.offer.dto.UpdateOfferRequest;
import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.repositories.OfferRepository;
import ma.affilatelink.offer.repositories.OfferSpecifications;
import ma.affilatelink.offer.services.implementation.OfferServiceImpl;
import ma.affilatelink.offer.repositories.TagRepository;
import ma.affilatelink.profil.api.ProfileApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private OfferMapper offerMapper;

    @Mock
    private ProfileApi profileApi;

    private OfferService offerService;

    private CreateOfferRequest createRequest;
    private Offer offer;
    private OfferResponse response;

    @BeforeEach
    void setUp() {
        offerService = new OfferServiceImpl(
                offerRepository,
                tagRepository,
                offerMapper,
                profileApi
        );
        createRequest = new CreateOfferRequest(
                1L,
                "Java Course",
                "A course about Java",
                "https://example.com/java",
                "TECH",
                Set.of()
        );

        offer = new Offer(
                1L,
                "Java Course",
                "A course about Java",
                "https://example.com/java",
                "TECH"
        );

        response = mock(OfferResponse.class);
    }

    @Test
    void shouldCreateOfferWhenAdvertiserIsActive() {
        when(profileApi.isAdvertiserActive(1L)).thenReturn(true);
        when(offerMapper.toEntity(createRequest)).thenReturn(offer);
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(response);

        OfferResponse result = offerService.createOffer(createRequest);

        assertSame(response, result);
        verify(profileApi).isAdvertiserActive(1L);
        verify(offerRepository).save(offer);
        verify(offerMapper).toEntity(createRequest);
        verify(offerMapper).toResponse(offer);
    }

    @Test
    void shouldRejectOfferWhenAdvertiserIsInactive() {
        when(profileApi.isAdvertiserActive(1L)).thenReturn(false);

        assertThrows(
                NoSuchElementException.class,
                () -> offerService.createOffer(createRequest)
        );

        verify(offerRepository, never()).save(any());
        verifyNoInteractions(offerMapper, tagRepository);
    }

    @Test
    void shouldUpdateExistingOffer() {
        UpdateOfferRequest request = new UpdateOfferRequest(
                "Updated title",
                "Updated description",
                "https://example.com/updated",
                "TECH",
                Set.of()
        );

        when(offerRepository.findById(10L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(response);

        OfferResponse result = offerService.updateOffer(10L, request);

        assertSame(response, result);
        verify(offerMapper).updateEntity(request, offer);
        verify(offerRepository).save(offer);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistingOffer() {
        UpdateOfferRequest request = mock(UpdateOfferRequest.class);
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> offerService.updateOffer(99L, request)
        );

        verify(offerRepository, never()).save(any());
        verifyNoInteractions(offerMapper);
    }

    @Test
    void shouldRetrieveOfferById() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerMapper.toResponse(offer)).thenReturn(response);

        OfferResponse result = offerService.getOfferById(1L);

        assertSame(response, result);
        verify(offerRepository).findById(1L);
        verify(offerMapper).toResponse(offer);
    }

    @Test
    void shouldThrowWhenRetrievingNonExistingOffer() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> offerService.getOfferById(99L)
        );

        verifyNoInteractions(offerMapper);
    }

    @Test
    void shouldSearchOffersUsingProvidedFiltersAndPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Offer> offerPage = new PageImpl<>(List.of(offer));

        when(offerRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(offerPage);

        when(offerMapper.toResponse(offer)).thenReturn(response);

        Page<OfferResponse> result = offerService.searchOffers(
                "TECH",
                1L,
                "Java",
                "course",
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().get(0));

        verify(offerRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
        verify(offerMapper).toResponse(offer);
    }

    @Test
    void shouldReturnEmptyPageWhenNoOffersMatchSearch() {
        Pageable pageable = PageRequest.of(0, 10);

        when(offerRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(Page.empty(pageable));

        Page<OfferResponse> result = offerService.searchOffers(
                "TECH",
                null,
                null,
                "unknown",
                pageable
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verifyNoInteractions(offerMapper);
    }
}
