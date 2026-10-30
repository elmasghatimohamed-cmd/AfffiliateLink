
package ma.affilatelink.offer.repositories;

import ma.affilatelink.offer.entities.Campaign;
import ma.affilatelink.offer.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByStatus(CampaignStatus status);

    Optional<Campaign> findByOffer_IdAndStatus(
            Long offerId,
            CampaignStatus status
    );

}
