package ma.affilatelink.profil.controllers;

import lombok.RequiredArgsConstructor;
import ma.affilatelink.profil.dto.PublisherRequest;
import ma.affilatelink.profil.dto.PublisherResponse;
import ma.affilatelink.profil.services.PublisherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping
    public List<PublisherResponse> findAll() {
        return publisherService.findAll();
    }

    @GetMapping("/{id}")
    public PublisherResponse findById(@PathVariable Long id) {
        return publisherService.findById(id);
    }

    @PostMapping
    public ResponseEntity<PublisherResponse> create(
            @Valid @RequestBody PublisherRequest request) {

        PublisherResponse response = publisherService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}