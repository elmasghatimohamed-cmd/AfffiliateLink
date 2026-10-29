
package ma.affilatelink.offer.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.affilatelink.common.ApiResponse;
import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.OfferResponse;
import ma.affilatelink.offer.dto.UpdateOfferRequest;
import ma.affilatelink.offer.services.OfferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    public ResponseEntity<List<OfferResponse>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferResponse> getOfferById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
