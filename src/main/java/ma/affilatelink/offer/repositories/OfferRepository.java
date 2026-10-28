package ma.affilatelink.offer.repositories;

import ma.affilatelink.offer.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {
}
