
package ma.affilatelink.offer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ma.affilatelink.offer.enums.PayoutModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record CampaignCreateRequest(

        @NotBlank(message = "Campaign name is required")
        @Size(max = 255, message = "Campaign name must not exceed 255 characters")
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
        @AssertTrue(message = "End date must be after start date")
        public boolean isDateRangeValid() {
                return startDate == null
                        || endDate == null
                        || endDate.isAfter(startDate);
        }
}
