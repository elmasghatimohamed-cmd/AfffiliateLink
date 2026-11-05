package ma.affilatelink.profil.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.affilatelink.common.exceptions.EmailAlreadyExistsException;
import ma.affilatelink.profil.repositories.ProfileRepository;
import ma.affilatelink.profil.dto.AdvertiserRequest;
import ma.affilatelink.profil.dto.AdvertiserResponse;
import ma.affilatelink.profil.entities.Advertiser;
import ma.affilatelink.profil.repositories.AdvertiserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertiserService {

    private final AdvertiserRepository advertiserRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public AdvertiserResponse create(AdvertiserRequest request) {
        log.info("Creating advertiser profile");

        if (profileRepository.existsByEmail(request.email())) {
            log.warn("Advertiser creation rejected: email already exists");

            throw new EmailAlreadyExistsException(request.email());
        }

        Advertiser advertiser = new Advertiser(
                request.companyName(),
                request.sector(),
                request.website(),
                request.email()
        );

        advertiser = advertiserRepository.save(advertiser);

        log.info(
                "Advertiser profile created successfully with ID: {}",
                advertiser.getId()
        );

        return toResponse(advertiser);
    }

    @Transactional(readOnly = true)
    public List<AdvertiserResponse> findAll() {
        log.debug("Retrieving all advertiser profiles");

        List<AdvertiserResponse> advertisers = advertiserRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        log.debug("Retrieved {} advertiser profiles", advertisers.size());

        return advertisers;
    }

    @Transactional(readOnly = true)
    public AdvertiserResponse findById(Long id) {
        log.debug("Retrieving advertiser profile with ID: {}", id);

        Advertiser advertiser = advertiserRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Advertiser not found with id: " + id
                        )
                );

        return toResponse(advertiser);
    }

    private AdvertiserResponse toResponse(Advertiser advertiser) {
        return new AdvertiserResponse(
                advertiser.getId(),
                advertiser.getEmail(),
                advertiser.getStatus(),
                advertiser.getCreatedAt(),
                advertiser.getCompanyName(),
                advertiser.getSector(),
                advertiser.getWebsite()
        );
    }
}