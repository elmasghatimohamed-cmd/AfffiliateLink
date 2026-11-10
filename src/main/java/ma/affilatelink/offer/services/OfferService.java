
package ma.affilatelink.offer.services;

import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.OfferResponse;
import ma.affilatelink.offer.dto.UpdateOfferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfferService {

    OfferResponse createOffer(CreateOfferRequest request);

    OfferResponse updateOffer(Long offerId, UpdateOfferRequest request);

    OfferResponse getOfferById(Long offerId);

    Page<OfferResponse> searchOffers(
            String niche,
            Long advertiserId,
            String tag,
            String keyword,
            Pageable pageable
    );

}
