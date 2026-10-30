
package ma.affilatelink.offer.services;

import lombok.RequiredArgsConstructor;
import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.OfferMapper;
import ma.affilatelink.offer.dto.OfferResponse;
import ma.affilatelink.offer.dto.UpdateOfferRequest;
import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.entities.Tag;
import ma.affilatelink.offer.repositories.OfferRepository;
import ma.affilatelink.offer.repositories.OfferSpecifications;
import ma.affilatelink.offer.repositories.TagRepository;

import java.util.List;
import java.util.NoSuchElementException;
import ma.affilatelink.profil.api.ProfileApi;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfferService {

    private final OfferRepository offerRepository;
    private final TagRepository tagRepository;
    private final OfferMapper offerMapper;
    private final ProfileApi profileApi;

    @Transactional
    public OfferResponse createOffer(CreateOfferRequest request) {
        if (!profileApi.isAdvertiserActive(request.advertiserId())) {
            throw new NoSuchElementException(
                    "Advertiser is not active or does not exist"
            );
        }

        Offer offer = offerMapper.toEntity(request);
        offer.getTags().addAll(resolveTags(request.tags()));

        Offer savedOffer = offerRepository.save(offer);

        return offerMapper.toResponse(savedOffer);
    }

    @Transactional
    public OfferResponse updateOffer(Long offerId, UpdateOfferRequest request) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Offer not found with id: " + offerId
                ));

        offerMapper.updateEntity(request, offer);

        offer.getTags().clear();
        offer.getTags().addAll(resolveTags(request.tags()));

        Offer savedOffer = offerRepository.save(offer);

        return offerMapper.toResponse(savedOffer);
    }

    private Set<Tag> resolveTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        return tagNames.stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toSet());
    }

    public OfferResponse getOfferById(Long offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Offer not found with id: " + offerId
                ));

        return offerMapper.toResponse(offer);
    }


    public Page<OfferResponse> searchOffers(
            String niche,
            Long advertiserId,
            String tag,
            String keyword,
            Pageable pageable
    ) {
        return offerRepository.findAll(
                OfferSpecifications.withFilters(
                        niche,
                        advertiserId,
                        tag,
                        keyword
                ),
                pageable
        ).map(offerMapper::toResponse);
    }

}
