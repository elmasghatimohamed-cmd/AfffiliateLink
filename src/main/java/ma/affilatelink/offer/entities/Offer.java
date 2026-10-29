package ma.affilatelink.offer.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advertiser_id", nullable = false)
    private Long advertiserId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "landing_url", nullable = false)
    private String landingUrl;

    @Column(name = "niche", nullable = false, length = 50)
    private String niche;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "offer_tags",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public Offer(Long advertiserId, String title, String description,
          String landingUrl, String niche) {
        this.advertiserId = advertiserId;
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.niche = niche;
    }

    public void updateOffer(
            String title,
            String description,
            String landingUrl,
            String niche
    ) {
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.niche = niche;
    }
}