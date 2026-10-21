package ma.affilatelink.profil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record PublisherRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 255, message = "First name must not exceed 255 characters")
        String firstname,

        @NotBlank(message = "Last name is required")
        @Size(max = 255, message = "Last name must not exceed 255 characters")
        String lastname,

        @NotNull(message = "Niche is required")
        Niche niche,

        @NotBlank(message = "Bio is required")
        @Size(max = 255, message = "Bio must not exceed 255 characters")
        String bio,

        @NotBlank(message = "Country is required")
        @Size(max = 255, message = "Country must not exceed 255 characters")
        String country

) {}
