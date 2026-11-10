package ma.affilatelink.profil.services;

import ma.affilatelink.common.exceptions.EmailAlreadyExistsException;
import ma.affilatelink.profil.dto.AdvertiserRequest;
import ma.affilatelink.profil.dto.AdvertiserResponse;
import ma.affilatelink.profil.entities.Advertiser;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.repositories.AdvertiserRepository;
import ma.affilatelink.profil.repositories.ProfileRepository;
import ma.affilatelink.profil.services.implementation.AdvertiserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdvertiserServiceTest {

    private AdvertiserRepository advertiserRepository;
    private ProfileRepository profileRepository;
    private AdvertiserService advertiserService;

    @BeforeEach
    void setUp() {
        advertiserRepository = mock(AdvertiserRepository.class);
        profileRepository = mock(ProfileRepository.class);

        advertiserService = new AdvertiserServiceImpl(
                advertiserRepository,
                profileRepository
        );
    }

    @Test
    void shouldCreateAdvertiserSuccessfully() {
        AdvertiserRequest request = createRequest();
        LocalDateTime createdAt = LocalDateTime.now();

        Advertiser savedAdvertiser = createAdvertiser(
                1L, request, createdAt
        );

        when(profileRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(advertiserRepository.save(any(Advertiser.class)))
                .thenReturn(savedAdvertiser);

        AdvertiserResponse response = advertiserService.create(request);

        ArgumentCaptor<Advertiser> captor =
                ArgumentCaptor.forClass(Advertiser.class);

        verify(advertiserRepository).save(captor.capture());

        Advertiser advertiserToSave = captor.getValue();

        assertEquals(request.email(), advertiserToSave.getEmail());
        assertEquals(request.companyName(), advertiserToSave.getCompanyName());
        assertEquals(request.sector(), advertiserToSave.getSector());
        assertEquals(request.website(), advertiserToSave.getWebsite());

        assertEquals(1L, response.id());
        assertEquals(request.email(), response.email());
        assertEquals(ProfileStatus.PENDING, response.status());
        assertEquals(createdAt, response.createdAt());
        assertEquals(request.companyName(), response.companyName());
        assertEquals(request.sector(), response.sector());
        assertEquals(request.website(), response.website());

        verify(profileRepository).existsByEmail(request.email());
    }

    @Test
    void shouldRejectCreationWhenEmailAlreadyExists() {
        AdvertiserRequest request = createRequest();

        when(profileRepository.existsByEmail(request.email()))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> advertiserService.create(request)
        );

        verify(profileRepository).existsByEmail(request.email());
        verifyNoInteractions(advertiserRepository);
    }

    @Test
    void shouldPropagateExceptionWhenSavingAdvertiserFails() {
        AdvertiserRequest request = createRequest();

        RuntimeException expectedException =
                new RuntimeException("Database unavailable");

        when(profileRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(advertiserRepository.save(any(Advertiser.class)))
                .thenThrow(expectedException);

        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> advertiserService.create(request)
        );

        assertSame(expectedException, actualException);

        verify(profileRepository).existsByEmail(request.email());
        verify(advertiserRepository).save(any(Advertiser.class));
    }

    @Test
    void shouldReturnAllAdvertisers() {
        LocalDateTime firstCreatedAt = LocalDateTime.now();
        LocalDateTime secondCreatedAt = firstCreatedAt.plusDays(1);

        Advertiser firstAdvertiser = createAdvertiser(
                1L, createRequest(), firstCreatedAt
        );

        AdvertiserRequest secondRequest = new AdvertiserRequest(
                "second@test.com",
                "Second Company",
                "Technology",
                "https://second-company.com"
        );

        Advertiser secondAdvertiser = createAdvertiser(
                2L, secondRequest, secondCreatedAt
        );

        when(advertiserRepository.findAll())
                .thenReturn(List.of(firstAdvertiser, secondAdvertiser));

        List<AdvertiserResponse> responses = advertiserService.findAll();

        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).id());
        assertEquals("advertiser@test.com", responses.get(0).email());
        assertEquals("First Company", responses.get(0).companyName());
        assertEquals("Retail", responses.get(0).sector());
        assertEquals(firstCreatedAt, responses.get(0).createdAt());

        assertEquals(2L, responses.get(1).id());
        assertEquals("second@test.com", responses.get(1).email());
        assertEquals("Second Company", responses.get(1).companyName());
        assertEquals("Technology", responses.get(1).sector());
        assertEquals(secondCreatedAt, responses.get(1).createdAt());

        verify(advertiserRepository).findAll();
        verifyNoMoreInteractions(advertiserRepository);
    }

    @Test
    void shouldReturnEmptyListWhenNoAdvertisersExist() {
        when(advertiserRepository.findAll()).thenReturn(List.of());

        List<AdvertiserResponse> responses = advertiserService.findAll();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(advertiserRepository).findAll();
    }

    @Test
    void shouldPropagateExceptionWhenRetrievingAllAdvertisersFails() {
        RuntimeException expectedException =
                new RuntimeException("Database unavailable");

        when(advertiserRepository.findAll())
                .thenThrow(expectedException);

        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> advertiserService.findAll()
        );

        assertSame(expectedException, actualException);
    }

    @Test
    void shouldReturnAdvertiserById() {
        AdvertiserRequest request = createRequest();
        LocalDateTime createdAt = LocalDateTime.now();

        Advertiser advertiser = createAdvertiser(
                1L, request, createdAt
        );

        when(advertiserRepository.findById(1L))
                .thenReturn(Optional.of(advertiser));

        AdvertiserResponse response = advertiserService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals(request.email(), response.email());
        assertEquals(ProfileStatus.PENDING, response.status());
        assertEquals(createdAt, response.createdAt());
        assertEquals(request.companyName(), response.companyName());
        assertEquals(request.sector(), response.sector());
        assertEquals(request.website(), response.website());

        verify(advertiserRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenAdvertiserDoesNotExist() {
        when(advertiserRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> advertiserService.findById(99L)
        );

        verify(advertiserRepository).findById(99L);
    }

    @Test
    void shouldPropagateExceptionWhenRetrievingAdvertiserFails() {
        RuntimeException expectedException =
                new RuntimeException("Database unavailable");

        when(advertiserRepository.findById(1L))
                .thenThrow(expectedException);

        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> advertiserService.findById(1L)
        );

        assertSame(expectedException, actualException);
    }

    private AdvertiserRequest createRequest() {
        return new AdvertiserRequest(
                "advertiser@test.com",
                "First Company",
                "Retail",
                "https://first-company.com"
        );
    }

    private Advertiser createAdvertiser(
            Long id,
            AdvertiserRequest request,
            LocalDateTime createdAt
    ) {
        Advertiser advertiser = mock(Advertiser.class);

        when(advertiser.getId()).thenReturn(id);
        when(advertiser.getEmail()).thenReturn(request.email());
        when(advertiser.getStatus()).thenReturn(ProfileStatus.PENDING);
        when(advertiser.getCreatedAt()).thenReturn(createdAt);
        when(advertiser.getCompanyName()).thenReturn(request.companyName());
        when(advertiser.getSector()).thenReturn(request.sector());
        when(advertiser.getWebsite()).thenReturn(request.website());

        return advertiser;
    }
}
