package ma.affilatelink.offer.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.affilatelink.offer.dto.CampaignCreateRequest;
import ma.affilatelink.offer.dto.CampaignResponse;
import ma.affilatelink.offer.dto.UpdateCampaignStatusRequest;
import ma.affilatelink.offer.enums.CampaignStatus;
import ma.affilatelink.offer.services.CampaignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
class CampaignController {

    private final CampaignService campaignService;

    @PostMapping("/api/offres/{id}/campaigns")
    public ResponseEntity<CampaignResponse> createCampaign(
            @PathVariable("id") Long offerId,
            @Valid @RequestBody CampaignCreateRequest request
    ) {
        CampaignResponse response =
                campaignService.createCampaign(offerId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/campaigns")
    public ResponseEntity<List<CampaignResponse>> getCampaigns(
            @RequestParam(required = false) String status
    ) {
        CampaignStatus campaignStatus = parseStatus(status);

        return ResponseEntity.ok(
                campaignService.getCampaigns(campaignStatus)
        );
    }

    @PatchMapping("/api/campaigns/{id}/status")
    public ResponseEntity<Void> updateCampaignStatus(
            @PathVariable("id") Long campaignId,
            @Valid @RequestBody UpdateCampaignStatusRequest request
    ) {
        campaignService.updateCampaignStatus(
                campaignId,
                request.status()
        );

        return ResponseEntity.noContent().build();
    }

    private CampaignStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return CampaignStatus.valueOf(
                    status.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid campaign status: " + status
            );
        }
    }
}
