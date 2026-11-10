package ma.affilatelink.offer.services;

import ma.affilatelink.offer.dto.CampaignCreateRequest;
import ma.affilatelink.offer.dto.CampaignResponse;
import ma.affilatelink.offer.enums.CampaignStatus;

import java.util.List;
import java.util.Optional;

public interface CampaignService {

    CampaignResponse createCampaign(
            Long offerId,
            CampaignCreateRequest request
    );

    List<CampaignResponse> getCampaigns(CampaignStatus status);

    void updateCampaignStatus(
            Long campaignId,
            CampaignStatus newStatus
    );

    Optional<CampaignResponse> getCampaign(Long campaignId);

}
