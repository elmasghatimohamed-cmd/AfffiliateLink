
package ma.affilatelink.profil.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.affilatelink.profil.dto.AdvertiserRequest;
import ma.affilatelink.profil.dto.AdvertiserResponse;
import ma.affilatelink.profil.services.AdvertiserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/advertisers")
public class AdvertiserController {

    private final AdvertiserService advertiserService;

    @GetMapping
    public List<AdvertiserResponse> findAll() {
        return advertiserService.findAll();
    }

    @GetMapping("/{id}")
    public AdvertiserResponse findById(@PathVariable Long id) {
        return advertiserService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AdvertiserResponse> create(
            @Valid @RequestBody AdvertiserRequest request) {

        AdvertiserResponse response = advertiserService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
