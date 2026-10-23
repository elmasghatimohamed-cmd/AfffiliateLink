
package ma.affilatelink.profil;

import java.time.LocalDateTime;

record AdvertiserResponse(
        Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt,
        String companyName,
        String sector,
        String website
) {}
