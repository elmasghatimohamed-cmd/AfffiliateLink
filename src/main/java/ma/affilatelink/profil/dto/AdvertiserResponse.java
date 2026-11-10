
package ma.affilatelink.profil.dto;

import ma.affilatelink.profil.enums.ProfileStatus;

import java.time.LocalDateTime;

public record AdvertiserResponse(
        Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt,
        String companyName,
        String sector,
        String website
) {}
