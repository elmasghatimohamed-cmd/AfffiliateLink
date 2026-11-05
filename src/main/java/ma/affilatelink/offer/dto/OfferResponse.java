package ma.affilatelink.offer.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record OfferResponse(
        Long id,
        Long advertiserId,
        String title,
        String description,
        String landingUrl,
        String niche,
        LocalDateTime createdAt,
        Set<String> tags,
        List<CampaignResponse> campaigns
) {}