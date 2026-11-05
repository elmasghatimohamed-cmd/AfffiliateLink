package ma.affilatelink.profil.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.repositories.AdvertiserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileApi {

    private final AdvertiserRepository advertiserRepository;

    @Transactional(readOnly = true)
    public boolean isAdvertiserActive(Long advertiserId) {
        log.debug("Checking active status for advertiser ID: {}", advertiserId);

        boolean active = advertiserRepository.existsByIdAndStatus(
                advertiserId,
                ProfileStatus.ACTIVE
        );

        log.debug(
                "Advertiser ID: {} active status: {}",
                advertiserId,
                active
        );

        return active;
    }
}