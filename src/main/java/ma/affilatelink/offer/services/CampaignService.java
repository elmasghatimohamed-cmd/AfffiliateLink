
package ma.affilatelink.offer.services;

import lombok.RequiredArgsConstructor;
import ma.affilatelink.common.exceptions.InvalidCampaignStatusException;
import ma.affilatelink.offer.dto.CampaignCreateRequest;
import ma.affilatelink.offer.dto.CampaignMapper;
import ma.affilatelink.offer.dto.CampaignResponse;
import ma.affilatelink.offer.entities.Campaign;
import ma.affilatelink.offer.entities.Offer;
import ma.affilatelink.offer.enums.CampaignStatus;
import ma.affilatelink.offer.enums.PayoutModel;
import ma.affilatelink.offer.repositories.CampaignRepository;
import ma.affilatelink.offer.repositories.OfferRepository;
import ma.affilatelink.profil.api.ProfileApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final OfferRepository offerRepository;
    private final CampaignMapper campaignMapper;
    private final ProfileApi profileApi;

    @Transactional
    public CampaignResponse createCampaign(
            Long offerId,
            CampaignCreateRequest request
    ) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Offer not found with id: " + offerId
                ));

        if (!profileApi.isAdvertiserActive(offer.getAdvertiserId())) {
            throw new NoSuchElementException(
                    "Advertiser is not active or does not exist"
            );
        }

        validateCampaign(request);

        Campaign campaign = new Campaign(
                request.name().trim(),
                request.startDate(),
                request.endDate(),
                request.budget(),
                request.payoutModel(),
                request.payoutValue(),
                CampaignStatus.DRAFT,
                offer
        );

        return campaignMapper.toResponse(
                campaignRepository.save(campaign)
        );
    }

    public List<CampaignResponse> getCampaigns(CampaignStatus status) {
        List<Campaign> campaigns = status == null
                ? campaignRepository.findAll()
                : campaignRepository.findByStatus(status);

        return campaigns.stream()
                .map(campaignMapper::toResponse)
                .toList();
    }

    @Transactional
    public void updateCampaignStatus(
            Long campaignId,
            CampaignStatus newStatus
    ) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Campaign not found with id: " + campaignId
                ));

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Campaign status is required"
            );
        }

        CampaignStatus currentStatus = campaign.getStatus();

        if (!isAllowedTransition(currentStatus, newStatus)) {
            throw new InvalidCampaignStatusException(
                    "Invalid campaign status transition: "
                            + currentStatus + " -> " + newStatus
            );
        }

        if (newStatus == CampaignStatus.ACTIVE) {
            if (campaign.getEndDate().isBefore(LocalDate.now())) {
                throw new InvalidCampaignStatusException(
                        "An expired campaign cannot be activated"
                );
            }

            campaignRepository.findByOffer_IdAndStatus(
                    campaign.getOffer().getId(),
                    CampaignStatus.ACTIVE
            ).filter(activeCampaign ->
                    !activeCampaign.getId().equals(campaign.getId())
            ).ifPresent(activeCampaign ->
                    activeCampaign.setStatus(CampaignStatus.PAUSED)
            );
        }

        campaign.setStatus(newStatus);
    }

    private boolean isAllowedTransition(
            CampaignStatus currentStatus,
            CampaignStatus newStatus
    ) {
        return switch (currentStatus) {
            case DRAFT -> newStatus == CampaignStatus.ACTIVE;
            case ACTIVE -> newStatus == CampaignStatus.PAUSED
                    || newStatus == CampaignStatus.ENDED;
            case PAUSED -> newStatus == CampaignStatus.ACTIVE
                    || newStatus == CampaignStatus.ENDED;
            case ENDED -> false;
        };
    }

    private void validateCampaign(CampaignCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Campaign request is required"
            );
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException(
                    "Campaign name is required"
            );
        }

        if (request.startDate() == null || request.endDate() == null) {
            throw new IllegalArgumentException(
                    "Campaign start date and end date are required"
            );
        }

        if (!request.endDate().isAfter(request.startDate())) {
            throw new IllegalArgumentException(
                    "End date must be after start date"
            );
        }

        if (request.budget() == null
                || request.budget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Budget must be greater than zero"
            );
        }

        if (request.payoutModel() == null
                || request.payoutValue() == null) {
            throw new IllegalArgumentException(
                    "Payout model and payout value are required"
            );
        }

        BigDecimal payoutValue = request.payoutValue();

        switch (request.payoutModel()) {
            case CPC, CPL, CPA_FIXED -> {
                if (payoutValue.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "Payout value must be greater than zero for "
                                    + request.payoutModel()
                    );
                }
            }
            case CPA_PERCENT -> {
                if (payoutValue.compareTo(BigDecimal.ZERO) < 0
                        || payoutValue.compareTo(
                        new BigDecimal("100")) > 0) {
                    throw new IllegalArgumentException(
                            "CPA_PERCENT must be between 0 and 100"
                    );
                }
            }
        }
    }
}
