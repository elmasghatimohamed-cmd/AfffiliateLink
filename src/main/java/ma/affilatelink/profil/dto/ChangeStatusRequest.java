
package ma.affilatelink.profil.dto;

import jakarta.validation.constraints.NotNull;
import ma.affilatelink.profil.enums.ProfileStatus;

public record ChangeStatusRequest(
        @NotNull(message = "Status is required")
        ProfileStatus status
) {}
