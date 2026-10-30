
package ma.affilatelink.offer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ma.affilatelink.offer.enums.PayoutModel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CampaignCreateRequest(

        @NotBlank
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal budget,

        @NotNull
        PayoutModel payoutModel,

        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal payoutValue

) {
}
