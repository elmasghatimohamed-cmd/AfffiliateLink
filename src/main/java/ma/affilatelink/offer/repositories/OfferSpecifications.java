package ma.affilatelink.offer.repositories;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.entities.Tag;
import org.springframework.data.jpa.domain.Specification;

public final class OfferSpecifications {

    private OfferSpecifications() {
    }

    public static Specification<Offer> withFilters(
            String niche,
            Long advertiserId,
            String tag,
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (niche != null && !niche.isBlank()) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("niche")),
                                niche.trim().toLowerCase()
                        )
                );
            }

            if (advertiserId != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                root.get("advertiserId"),
                                advertiserId
                        )
                );
            }

            if (tag != null && !tag.isBlank()) {
                Join<Offer, Tag> tagJoin = root.join("tags", JoinType.INNER);

                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(tagJoin.get("name")),
                                tag.trim().toLowerCase()
                        )
                );

                query.distinct(true);
            }

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";

                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("title")),
                                        pattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("description")),
                                        pattern
                                )
                        )
                );
            }

            return predicates;
        };
    }
}
