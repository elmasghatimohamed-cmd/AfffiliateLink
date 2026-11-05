package ma.affilatelink.profil.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.affilatelink.common.exceptions.InvalidProfileStatusException;
import ma.affilatelink.profil.entities.Profile;
import ma.affilatelink.profil.repositories.ProfileRepository;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.events.ProfileSuspended;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void changeStatus(Long id, ProfileStatus newStatus) {
        log.info(
                "Changing status of profile ID: {} to {}",
                id,
                newStatus
        );

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Profile not found with id: " + id
                        )
                );

        ProfileStatus currentStatus = profile.getStatus();

        switch (newStatus) {
            case ACTIVE -> profile.activate();

            case SUSPENDED -> {
                profile.suspend();

                eventPublisher.publishEvent(
                        new ProfileSuspended(profile.getId())
                );

                log.info(
                        "Suspension event published for profile ID: {}",
                        id
                );
            }

            case PENDING -> throw new InvalidProfileStatusException(
                    "A profile cannot transition to PENDING"
            );
        }

        log.info(
                "Profile ID: {} status changed from {} to {}",
                id,
                currentStatus,
                newStatus
        );
    }
}