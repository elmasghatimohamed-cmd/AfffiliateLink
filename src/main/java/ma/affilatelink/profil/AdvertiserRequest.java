
package ma.affilatelink.profil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

record AdvertiserRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Company name is required")
        @Size(max = 255, message = "Company name must not exceed 255 characters")
        String companyName,

        @NotBlank(message = "Sector is required")
        @Size(max = 255, message = "Sector must not exceed 255 characters")
        String sector,

        @NotBlank(message = "Website is required")
        @URL(message = "Website must be a valid URL")
        @Size(max = 255, message = "Website must not exceed 255 characters")
        String website

) {}
