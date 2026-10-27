
package ma.affilatelink.profil.api;

import lombok.RequiredArgsConstructor;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.repositories.AdvertiserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileApi {

    private final AdvertiserRepository advertiserRepository;

    @Transactional(readOnly = true)
    public boolean isAdvertiserActive(Long advertiserId) {
        return advertiserRepository.existsByIdAndStatus(
                advertiserId,
                ProfileStatus.ACTIVE
        );
    }
}
