package ma.affilatelink.profil.dto;



import ma.affilatelink.profil.enums.Niche;
import ma.affilatelink.profil.enums.ProfileStatus;

import java.time.LocalDateTime;

public record PublisherResponse(
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