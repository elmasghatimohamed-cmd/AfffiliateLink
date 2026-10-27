
package ma.affilatelink.profil.services;

import ma.affilatelink.common.exceptions.InvalidProfileStatusException;
import ma.affilatelink.profil.entities.Profile;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.events.ProfileSuspended;
import ma.affilatelink.profil.repositories.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private ProfileRepository profileRepository;
    private ApplicationEventPublisher eventPublisher;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        adminService = new AdminService(profileRepository, eventPublisher);
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
    void shouldNotPublishEventWhenTargetStatusIsPending() {
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

        verifyNoInteractions(eventPublisher);
    }
}
