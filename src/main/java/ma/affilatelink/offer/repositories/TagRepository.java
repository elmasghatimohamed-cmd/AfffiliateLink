package ma.affilatelink.offer.repositories;

import ma.affilatelink.offer.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
