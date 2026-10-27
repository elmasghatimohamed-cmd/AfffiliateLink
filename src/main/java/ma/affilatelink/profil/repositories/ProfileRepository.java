package ma.affilatelink.profil.repositories;

import ma.affilatelink.profil.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByEmail(String email);
}
