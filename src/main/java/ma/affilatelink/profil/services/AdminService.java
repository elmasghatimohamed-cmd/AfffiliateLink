
package ma.affilatelink.profil.services;

import lombok.RequiredArgsConstructor;
import ma.affilatelink.common.exceptions.InvalidProfileStatusException;
import ma.affilatelink.profil.entities.Profile;
import ma.affilatelink.profil.repositories.ProfileRepository;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.events.ProfileSuspended;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void changeStatus(Long id, ProfileStatus newStatus) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Profile not found with id: " + id
                        )
                );

        switch (newStatus) {
            case ACTIVE -> profile.activate();
            case SUSPENDED -> {
                profile.suspend();
                eventPublisher.publishEvent(new ProfileSuspended(profile.getId()));
            }
            case PENDING -> throw new InvalidProfileStatusException(
                    "A profile cannot transition to PENDING"
            );
        }
    }
}
