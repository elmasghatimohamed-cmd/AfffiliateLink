package ma.affilatelink.offer.services;

import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.OfferMapper;
import ma.affilatelink.offer.dto.OfferResponse;
import ma.affilatelink.offer.dto.UpdateOfferRequest;
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

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferTagManagementTest {

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

    private Offer offer;

    @BeforeEach
    void setUp() {
        offer = new Offer(
                1L,
                "Java Course",
                "A course about Java",
                "https://example.com/java",
                "TECH"
        );
    }

    @Test
    void shouldCreateAndAssociateNewTags() {
        CreateOfferRequest request = new CreateOfferRequest(
                1L, "Java Course", "Description",
                "https://example.com", "TECH",
                Set.of("Java", "Spring")
        );

        when(profileApi.isAdvertiserActive(1L)).thenReturn(true);
        when(offerMapper.toEntity(request)).thenReturn(offer);
        when(tagRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(mock(OfferResponse.class));

        offerService.createOffer(request);

        assertEquals(2, offer.getTags().size());
        assertTrue(offer.getTags().stream()
                .anyMatch(tag -> tag.getName().equals("Java")));
        assertTrue(offer.getTags().stream()
                .anyMatch(tag -> tag.getName().equals("Spring")));

        verify(tagRepository, times(2)).save(any(Tag.class));
    }

    @Test
    void shouldReuseExistingTagInsteadOfCreatingAnother() {
        Tag existingTag = new Tag("Java");

        CreateOfferRequest request = new CreateOfferRequest(
                1L, "Java Course", "Description",
                "https://example.com", "TECH",
                Set.of("Java")
        );

        when(profileApi.isAdvertiserActive(1L)).thenReturn(true);
        when(offerMapper.toEntity(request)).thenReturn(offer);
        when(tagRepository.findByName("Java"))
                .thenReturn(Optional.of(existingTag));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(mock(OfferResponse.class));

        offerService.createOffer(request);

        assertTrue(offer.getTags().contains(existingTag));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void shouldTrimTagNamesBeforeLookup() {
        CreateOfferRequest request = new CreateOfferRequest(
                1L, "Java Course", "Description",
                "https://example.com", "TECH",
                Set.of(" Java ")
        );

        when(profileApi.isAdvertiserActive(1L)).thenReturn(true);
        when(offerMapper.toEntity(request)).thenReturn(offer);
        when(tagRepository.findByName("Java")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(mock(OfferResponse.class));

        offerService.createOffer(request);

        verify(tagRepository).findByName("Java");
    }

    @Test
    void shouldIgnoreBlankTagNames() {
        CreateOfferRequest request = new CreateOfferRequest(
                1L, "Java Course", "Description",
                "https://example.com", "TECH",
                Set.of(" ")
        );

        when(profileApi.isAdvertiserActive(1L)).thenReturn(true);
        when(offerMapper.toEntity(request)).thenReturn(offer);
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(mock(OfferResponse.class));

        offerService.createOffer(request);

        assertTrue(offer.getTags().isEmpty());
        verifyNoInteractions(tagRepository);
    }

    @Test
    void shouldClearExistingTagsWhenUpdatingWithEmptyTags() {
        Tag oldTag = new Tag("Old");
        offer.getTags().add(oldTag);

        UpdateOfferRequest request = new UpdateOfferRequest(
                "Updated title", "Updated description",
                "https://example.com/updated", "TECH",
                Set.of()
        );

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(mock(OfferResponse.class));

        offerService.updateOffer(1L, request);

        assertTrue(offer.getTags().isEmpty());
        verifyNoInteractions(tagRepository);
    }

    @Test
    void shouldReplaceExistingTagsWhenUpdating() {
        Tag oldTag = new Tag("Old");
        Tag newTag = new Tag("New");
        offer.getTags().add(oldTag);

        UpdateOfferRequest request = new UpdateOfferRequest(
                "Updated title", "Updated description",
                "https://example.com/updated", "TECH",
                Set.of("New")
        );

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(tagRepository.findByName("New")).thenReturn(Optional.of(newTag));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponse(offer)).thenReturn(mock(OfferResponse.class));

        offerService.updateOffer(1L, request);

        assertEquals(1, offer.getTags().size());
        assertTrue(offer.getTags().contains(newTag));
        assertFalse(offer.getTags().contains(oldTag));
    }
}
