package ma.affilatelink.offer.api;

import lombok.RequiredArgsConstructor;
import ma.affilatelink.offer.dto.CampaignResponse;
import ma.affilatelink.offer.services.CampaignService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferApi {

    private final CampaignService campaignService;

    public Optional<CampaignResponse> getCampaign(Long campaignId) {
        return campaignService.getCampaign(campaignId);
    }
}