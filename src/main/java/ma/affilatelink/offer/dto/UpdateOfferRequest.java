package ma.affilatelink.offer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

public record UpdateOfferRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @NotBlank(message = "Landing URL is required")
        @URL(message = "Landing URL must be a valid URL")
        @Size(max = 255, message = "Landing URL must not exceed 255 characters")
        String landingUrl,

        @NotBlank(message = "Niche is required")
        @Size(max = 50, message = "Niche must not exceed 50 characters")
        String niche,

        Set<@NotBlank(message = "Tag name must not be blank")
        @Size(max = 255, message = "Tag name must not exceed 255 characters") String> tags

) {}
