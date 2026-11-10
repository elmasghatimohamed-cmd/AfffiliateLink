package ma.affilatelink.offer.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateOfferRequestValidationTest {

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
    void shouldAcceptValidOfferRequest() {
        CreateOfferRequest request = new CreateOfferRequest(
                1L,
                "Spring Boot Course",
                "Learn Spring Boot",
                "https://example.com/course",
                "TECH",
                Set.of("Java", "Spring")
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectNonPositiveAdvertiserId() {
        CreateOfferRequest request = new CreateOfferRequest(
                0L,
                "Spring Boot Course",
                "Learn Spring Boot",
                "https://example.com/course",
                "TECH",
                Set.of("Java")
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("advertiserId")));
    }

    @Test
    void shouldRejectInvalidLandingUrl() {
        CreateOfferRequest request = new CreateOfferRequest(
                1L,
                "Spring Boot Course",
                "Learn Spring Boot",
                "not-a-valid-url",
                "TECH",
                Set.of("Java")
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("landingUrl")));
    }
}