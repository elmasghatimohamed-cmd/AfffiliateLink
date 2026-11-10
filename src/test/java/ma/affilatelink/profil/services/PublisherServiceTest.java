package ma.affilatelink.profil.services;

import ma.affilatelink.common.exceptions.EmailAlreadyExistsException;
import ma.affilatelink.profil.dto.PublisherRequest;
import ma.affilatelink.profil.dto.PublisherResponse;
import ma.affilatelink.profil.entities.Publisher;
import ma.affilatelink.profil.enums.Niche;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.repositories.ProfileRepository;
import ma.affilatelink.profil.repositories.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PublisherServiceTest {

    private PublisherRepository publisherRepository;
    private ProfileRepository profileRepository;
    private PublisherService publisherService;

    @BeforeEach
    void setUp() {
        publisherRepository = mock(PublisherRepository.class);
        profileRepository = mock(ProfileRepository.class);

        publisherService = new PublisherService(
                publisherRepository,
                profileRepository
        );
    }

    @Test
    void shouldCreatePublisherSuccessfully() {
        // Arrange
        PublisherRequest request = createRequest();
        LocalDateTime createdAt = LocalDateTime.now();

        Publisher savedPublisher = createPublisher(
                1L, request, createdAt
        );

        when(profileRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(publisherRepository.save(any(Publisher.class)))
                .thenReturn(savedPublisher);

        // Act
        PublisherResponse response = publisherService.create(request);

        // Assert
        ArgumentCaptor<Publisher> captor =
                ArgumentCaptor.forClass(Publisher.class);

        verify(publisherRepository).save(captor.capture());

        Publisher publisherToSave = captor.getValue();

        assertEquals(request.email(), publisherToSave.getEmail());
        assertEquals(request.firstname(), publisherToSave.getFirstname());
        assertEquals(request.lastname(), publisherToSave.getLastname());
        assertEquals(request.niche(), publisherToSave.getNiche());
        assertEquals(request.bio(), publisherToSave.getBio());
        assertEquals(request.country(), publisherToSave.getCountry());

        assertEquals(1L, response.id());
        assertEquals(request.email(), response.email());
        assertEquals(ProfileStatus.PENDING, response.status());
        assertEquals(createdAt, response.createdAt());
        assertEquals(request.firstname(), response.firstname());
        assertEquals(request.lastname(), response.lastname());
        assertEquals(request.niche(), response.niche());
        assertEquals(request.bio(), response.bio());
        assertEquals(request.country(), response.country());

        verify(profileRepository).existsByEmail(request.email());
    }

    @Test
    void shouldRejectCreationWhenEmailAlreadyExists() {
        // Arrange
        PublisherRequest request = createRequest();

        when(profileRepository.existsByEmail(request.email()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> publisherService.create(request)
        );

        verify(profileRepository).existsByEmail(request.email());
        verifyNoInteractions(publisherRepository);
    }

    @Test
    void shouldPropagateExceptionWhenSavingPublisherFails() {
        // Arrange
        PublisherRequest request = createRequest();
        RuntimeException expectedException =
                new RuntimeException("Database unavailable");

        when(profileRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(publisherRepository.save(any(Publisher.class)))
                .thenThrow(expectedException);

        // Act
        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> publisherService.create(request)
        );

        // Assert
        assertSame(expectedException, actualException);

        verify(profileRepository).existsByEmail(request.email());
        verify(publisherRepository).save(any(Publisher.class));
    }

    @Test
    void shouldReturnAllPublishers() {
        // Arrange
        LocalDateTime firstCreatedAt = LocalDateTime.now();
        LocalDateTime secondCreatedAt = firstCreatedAt.plusDays(1);

        Publisher firstPublisher = createPublisher(
                1L, createRequest(), firstCreatedAt
        );

        PublisherRequest secondRequest = new PublisherRequest(
                "second@test.com",
                "John",
                "Doe",
                Niche.TECH,
                "Tech blogger",
                "France"
        );

        Publisher secondPublisher = createPublisher(
                2L, secondRequest, secondCreatedAt
        );

        when(publisherRepository.findAll())
                .thenReturn(List.of(firstPublisher, secondPublisher));

        // Act
        List<PublisherResponse> responses = publisherService.findAll();

        // Assert
        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).id());
        assertEquals("publisher@test.com", responses.get(0).email());
        assertEquals("Mohamed", responses.get(0).firstname());
        assertEquals("El Masghati", responses.get(0).lastname());
        assertEquals(Niche.BEAUTY, responses.get(0).niche());
        assertEquals(firstCreatedAt, responses.get(0).createdAt());

        assertEquals(2L, responses.get(1).id());
        assertEquals("second@test.com", responses.get(1).email());
        assertEquals("John", responses.get(1).firstname());
        assertEquals("Doe", responses.get(1).lastname());
        assertEquals(Niche.TECH, responses.get(1).niche());
        assertEquals(secondCreatedAt, responses.get(1).createdAt());

        verify(publisherRepository).findAll();
        verifyNoMoreInteractions(publisherRepository);
    }

    @Test
    void shouldReturnEmptyListWhenNoPublishersExist() {
        // Arrange
        when(publisherRepository.findAll()).thenReturn(List.of());

        // Act
        List<PublisherResponse> responses = publisherService.findAll();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(publisherRepository).findAll();
    }

    @Test
    void shouldPropagateExceptionWhenRetrievingAllPublishersFails() {
        // Arrange
        RuntimeException expectedException =
                new RuntimeException("Database unavailable");

        when(publisherRepository.findAll())
                .thenThrow(expectedException);

        // Act
        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> publisherService.findAll()
        );

        // Assert
        assertSame(expectedException, actualException);
    }

    @Test
    void shouldReturnPublisherById() {
        // Arrange
        PublisherRequest request = createRequest();
        LocalDateTime createdAt = LocalDateTime.now();

        Publisher publisher = createPublisher(1L, request, createdAt);

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        // Act
        PublisherResponse response = publisherService.findById(1L);

        // Assert
        assertEquals(1L, response.id());
        assertEquals(request.email(), response.email());
        assertEquals(ProfileStatus.PENDING, response.status());
        assertEquals(createdAt, response.createdAt());
        assertEquals(request.firstname(), response.firstname());
        assertEquals(request.lastname(), response.lastname());
        assertEquals(request.niche(), response.niche());
        assertEquals(request.bio(), response.bio());
        assertEquals(request.country(), response.country());

        verify(publisherRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenPublisherDoesNotExist() {
        // Arrange
        when(publisherRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                NoSuchElementException.class,
                () -> publisherService.findById(99L)
        );

        verify(publisherRepository).findById(99L);
    }

    @Test
    void shouldPropagateExceptionWhenRetrievingPublisherFails() {
        // Arrange
        RuntimeException expectedException =
                new RuntimeException("Database unavailable");

        when(publisherRepository.findById(1L))
                .thenThrow(expectedException);

        // Act
        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> publisherService.findById(1L)
        );

        // Assert
        assertSame(expectedException, actualException);
    }

    private PublisherRequest createRequest() {
        return new PublisherRequest(
                "publisher@test.com",
                "Mohamed",
                "El Masghati",
                Niche.BEAUTY,
                "Content creator",
                "Morocco"
        );
    }

    private Publisher createPublisher(
            Long id,
            PublisherRequest request,
            LocalDateTime createdAt
    ) {
        Publisher publisher = mock(Publisher.class);

        when(publisher.getId()).thenReturn(id);
        when(publisher.getEmail()).thenReturn(request.email());
        when(publisher.getStatus()).thenReturn(ProfileStatus.PENDING);
        when(publisher.getCreatedAt()).thenReturn(createdAt);
        when(publisher.getFirstname()).thenReturn(request.firstname());
        when(publisher.getLastname()).thenReturn(request.lastname());
        when(publisher.getNiche()).thenReturn(request.niche());
        when(publisher.getBio()).thenReturn(request.bio());
        when(publisher.getCountry()).thenReturn(request.country());

        return publisher;
    }
}
