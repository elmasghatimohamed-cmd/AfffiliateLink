package ma.affilatelink.offer.repositories;

import ma.affilatelink.offer.entities.Offer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long>,
        JpaSpecificationExecutor<Offer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM Offer o
            WHERE o.id = (
                SELECT c.offer.id
                FROM Campaign c
                WHERE c.id = :campaignId
            )
            """)
    Optional<Offer> findByCampaignIdForUpdate(
            @Param("campaignId") Long campaignId
    );
}