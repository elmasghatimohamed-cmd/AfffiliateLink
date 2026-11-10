
package ma.affilatelink.offer.dto;

import jakarta.validation.constraints.NotNull;
import ma.affilatelink.offer.enums.CampaignStatus;

public record UpdateCampaignStatusRequest(
        @NotNull CampaignStatus status
) {
}
