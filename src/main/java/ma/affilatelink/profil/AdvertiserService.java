
package ma.affilatelink.profil;

import lombok.RequiredArgsConstructor;
import ma.affilatelink.common.EmailAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdvertiserService {

    private final AdvertiserRepository advertiserRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public AdvertiserResponse create(AdvertiserRequest request) {

        if (profileRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        Advertiser advertiser = new Advertiser(
                request.companyName(),
                request.sector(),
                request.website(),
                request.email()
        );

        advertiser = advertiserRepository.save(advertiser);

        return toResponse(advertiser);
    }

    @Transactional(readOnly = true)
    public List<AdvertiserResponse> findAll() {
        return advertiserRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdvertiserResponse findById(Long id) {
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
