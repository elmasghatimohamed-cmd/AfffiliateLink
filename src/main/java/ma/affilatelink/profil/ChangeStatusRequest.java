
package ma.affilatelink.profil;

import jakarta.validation.constraints.NotNull;

record ChangeStatusRequest(
        @NotNull(message = "Status is required")
        ProfileStatus status
) {}
