package ma.affilatelink.offer.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CampaignCreateRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidCampaignRequest() {
        CampaignCreateRequest request = new CampaignCreateRequest(
                "Spring Boot Campaign",
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 12, 1),
                new BigDecimal("1000.00"),
                ma.affilatelink.offer.enums.PayoutModel.CPC,
                new BigDecimal("2.50")
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        CampaignCreateRequest request = new CampaignCreateRequest(
                "Invalid Campaign",
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 11, 1),
                new BigDecimal("1000.00"),
                ma.affilatelink.offer.enums.PayoutModel.CPC,
                new BigDecimal("2.50")
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectBlankCampaignName() {
        CampaignCreateRequest request = new CampaignCreateRequest(
                " ",
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 12, 1),
                new BigDecimal("1000.00"),
                ma.affilatelink.offer.enums.PayoutModel.CPC,
                new BigDecimal("2.50")
        );

        assertFalse(validator.validate(request).isEmpty());
    }
}