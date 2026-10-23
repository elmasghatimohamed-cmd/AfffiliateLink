
package ma.affilatelink.profil;

import lombok.RequiredArgsConstructor;
import ma.affilatelink.common.InvalidProfileStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfileRepository profileRepository;

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
            case SUSPENDED -> profile.suspend();
            case PENDING -> throw new InvalidProfileStatusException(
                    "A profile cannot transition to PENDING"
            );
        }
    }
}
