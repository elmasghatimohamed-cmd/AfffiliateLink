package ma.affilatelink.offer.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.OfferResponse;
import ma.affilatelink.offer.dto.UpdateOfferRequest;
import ma.affilatelink.offer.services.OfferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<OfferResponse> createOffer(
            @Valid @RequestBody CreateOfferRequest request
    ) {
        OfferResponse response = offerService.createOffer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferResponse> updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOfferRequest request
    ) {
        return ResponseEntity.ok(offerService.updateOffer(id, request));
    }


    @GetMapping
    public Page<OfferResponse> searchOffers(
            @RequestParam(required = false) String niche,
            @RequestParam(required = false) Long advertiserId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return offerService.searchOffers(
                niche,
                advertiserId,
                tag,
                keyword,
                pageable
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<OfferResponse> getOfferById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }
}
