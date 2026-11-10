package ma.affilatelink.profil.services;

import ma.affilatelink.common.exceptions.InvalidProfileStatusException;
import ma.affilatelink.profil.entities.Profile;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.events.ProfileSuspended;
import ma.affilatelink.profil.repositories.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private ProfileRepository profileRepository;
    private ApplicationEventPublisher eventPublisher;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        adminService = new AdminService(
                profileRepository,
                eventPublisher
        );
    }

    @Test
    void shouldActivateProfileSuccessfully() {
        Long profileId = 1L;
        Profile profile = mock(Profile.class);

        when(profileRepository.findById(profileId))
                .thenReturn(Optional.of(profile));

        adminService.changeStatus(profileId, ProfileStatus.ACTIVE);

        verify(profileRepository).findById(profileId);
        verify(profile).activate();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldPublishEventWhenProfileIsSuspended() {
        Long profileId = 1L;
        Profile profile = mock(Profile.class);

        when(profileRepository.findById(profileId))
                .thenReturn(Optional.of(profile));

        when(profile.getId()).thenReturn(profileId);

        adminService.changeStatus(profileId, ProfileStatus.SUSPENDED);

        verify(profile).suspend();

        verify(eventPublisher).publishEvent(
                new ProfileSuspended(profileId)
        );
    }

    @Test
    void shouldRejectTransitionToPending() {
        Long profileId = 1L;
        Profile profile = mock(Profile.class);

        when(profileRepository.findById(profileId))
                .thenReturn(Optional.of(profile));

        assertThrows(
                InvalidProfileStatusException.class,
                () -> adminService.changeStatus(
                        profileId,
                        ProfileStatus.PENDING
                )
        );

        verify(profile, never()).activate();
        verify(profile, never()).suspend();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldThrowExceptionWhenProfileDoesNotExist() {
        Long profileId = 99L;

        when(profileRepository.findById(profileId))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> adminService.changeStatus(
                        profileId,
                        ProfileStatus.ACTIVE
                )
        );

        verify(profileRepository).findById(profileId);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldNotPublishEventWhenSuspensionFails() {
        Long profileId = 1L;
        Profile profile = mock(Profile.class);

        InvalidProfileStatusException expectedException =
                new InvalidProfileStatusException("Invalid transition");

        when(profileRepository.findById(profileId))
                .thenReturn(Optional.of(profile));

        doThrow(expectedException).when(profile).suspend();

        InvalidProfileStatusException actualException = assertThrows(
                InvalidProfileStatusException.class,
                () -> adminService.changeStatus(
                        profileId,
                        ProfileStatus.SUSPENDED
                )
        );

        assertSame(expectedException, actualException);

        verify(profile).suspend();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldPropagateExceptionWhenProfileLookupFails() {
        Long profileId = 1L;

        RuntimeException expectedException =
                new RuntimeException("Database unavailable");

        when(profileRepository.findById(profileId))
                .thenThrow(expectedException);

        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> adminService.changeStatus(
                        profileId,
                        ProfileStatus.ACTIVE
                )
        );

        assertSame(expectedException, actualException);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldPropagateExceptionWhenActivationFails() {
        Long profileId = 1L;
        Profile profile = mock(Profile.class);

        InvalidProfileStatusException expectedException =
                new InvalidProfileStatusException("Invalid transition");

        when(profileRepository.findById(profileId))
                .thenReturn(Optional.of(profile));

        doThrow(expectedException).when(profile).activate();

        InvalidProfileStatusException actualException = assertThrows(
                InvalidProfileStatusException.class,
                () -> adminService.changeStatus(
                        profileId,
                        ProfileStatus.ACTIVE
                )
        );

        assertSame(expectedException, actualException);

        verify(profile).activate();
        verifyNoInteractions(eventPublisher);
    }
}
