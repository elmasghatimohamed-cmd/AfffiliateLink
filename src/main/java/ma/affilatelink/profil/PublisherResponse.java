package ma.affilatelink.profil;



import java.time.LocalDateTime;

record PublisherResponse(
        Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt,
        String firstname,
        String lastname,
        Niche niche,
        String bio,
        String country
) {}