package ma.affilatelink.offer.repositories;

import ma.affilatelink.offer.entities.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
}
