
package ma.affilatelink.offer.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.OfferMapper;
import ma.affilatelink.offer.dto.OfferResponse;
import ma.affilatelink.offer.dto.UpdateOfferRequest;
import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.entities.Tag;
import ma.affilatelink.offer.repositories.OfferRepository;
import ma.affilatelink.offer.repositories.OfferSpecifications;
import ma.affilatelink.offer.repositories.TagRepository;

import java.util.NoSuchElementException;
import ma.affilatelink.profil.api.ProfileApi;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("Creating offer for advertiser ID: {}", request.advertiserId());
        if (!profileApi.isAdvertiserActive(request.advertiserId())) {
            throw new NoSuchElementException(
                    "Advertiser is not active or does not exist"
            );
        }

        Offer offer = offerMapper.toEntity(request);
        offer.getTags().addAll(resolveTags(request.tags()));

        Offer savedOffer = offerRepository.save(offer);
        log.info("Offer created successfully with ID: {}", savedOffer.getId());
        return offerMapper.toResponse(savedOffer);
    }

    @Transactional
    public OfferResponse updateOffer(Long offerId, UpdateOfferRequest request) {
        log.info("Updating offer with ID: {}", offerId);
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Offer not found with id: " + offerId
                ));

        offerMapper.updateEntity(request, offer);

        offer.getTags().clear();
        offer.getTags().addAll(resolveTags(request.tags()));

        Offer savedOffer = offerRepository.save(offer);
        log.info("Offer updated successfully with ID: {}", savedOffer.getId());
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
        log.debug("Retrieving offer with ID: {}", offerId);
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
        log.debug(
                "Searching offers with niche: {}, advertiserId: {}, tag: {}, keyword: {}",
                niche,
                advertiserId,
                tag,
                keyword
        );
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
