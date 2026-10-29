
package ma.affilatelink.offer.services;

import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.OfferMapper;
import ma.affilatelink.offer.dto.OfferResponse;
import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.entities.Tag;
import ma.affilatelink.offer.repositories.OfferRepository;
import ma.affilatelink.offer.repositories.TagRepository;
import ma.affilatelink.profil.api.ProfileApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;
import ma.affilatelink.offer.dto.UpdateOfferRequest;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private OfferService offerService;

    private CreateOfferRequest request;
    private Offer offer;
    private OfferResponse response;

    @BeforeEach
    void setUp() {
        request = new CreateOfferRequest(
                1L,
                "Java Course",
                "A course about Java",
                "https://example.com/java",
                "TECH",
                Set.of("Java")
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
        when(offerMapper.toEntity(request)).thenReturn(offer);
        when(tagRepository.findByName("Java")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(response);

        OfferResponse result = offerService.createOffer(request);

        assertSame(response, result);
        assertTrue(offer.getTags().stream()
                .anyMatch(tag -> tag.getName().equals("Java")));

        verify(profileApi).isAdvertiserActive(1L);
        verify(offerRepository).save(offer);
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void shouldRejectOfferWhenAdvertiserIsInactive() {
        when(profileApi.isAdvertiserActive(1L)).thenReturn(false);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> offerService.createOffer(request)
        );

        assertTrue(exception.getMessage()
                .contains("Advertiser is not active or does not exist"));

        verify(offerRepository, never()).save(any(Offer.class));
        verifyNoInteractions(offerMapper, tagRepository);
    }

    @Test
    void shouldThrowWhenOfferDoesNotExist() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> offerService.updateOffer(99L, mock(UpdateOfferRequest.class))
        );

        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void shouldReuseExistingTagWhenCreatingOffer() {
        Tag existingTag = new Tag("Java");

        when(profileApi.isAdvertiserActive(1L)).thenReturn(true);
        when(offerMapper.toEntity(request)).thenReturn(offer);
        when(tagRepository.findByName("Java")).thenReturn(Optional.of(existingTag));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(response);

        offerService.createOffer(request);

        assertTrue(offer.getTags().contains(existingTag));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void shouldTrimTagNamesBeforeLookingThemUp() {
        CreateOfferRequest requestWithSpaces = new CreateOfferRequest(
                1L,
                "Java Course",
                "A course about Java",
                "https://example.com/java",
                "TECH",
                Set.of(" Java ")
        );

        when(profileApi.isAdvertiserActive(1L)).thenReturn(true);
        when(offerMapper.toEntity(requestWithSpaces)).thenReturn(offer);
        when(tagRepository.findByName("Java")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(response);

        offerService.createOffer(requestWithSpaces);

        verify(tagRepository).findByName("Java");
        verify(tagRepository).save(any(Tag.class));
    }
}
