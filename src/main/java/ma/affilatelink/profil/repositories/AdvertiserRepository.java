
package ma.affilatelink.profil.repositories;

import ma.affilatelink.profil.entities.Advertiser;
import ma.affilatelink.profil.enums.ProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertiserRepository extends JpaRepository<Advertiser, Long> {
    boolean existsByIdAndStatus(Long id, ProfileStatus status);
}
