package ma.affilatelink.common.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import ma.affilatelink.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest();

        mockRequest.setRequestURI("/api/offers/99");
        request = mockRequest;
    }

    @Test
    void shouldReturn404WhenResourceIsNotFound() {
        ResponseEntity<ApiResponse> response =
                handler.handleNotFound(
                        new NoSuchElementException("Offer not found"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message())
                .isEqualTo("Offer not found");
        assertThat(response.getBody().path())
                .isEqualTo("/api/offers/99");
    }

    @Test
    void shouldReturn409WhenBusinessConflictOccurs() {
        ResponseEntity<ApiResponse> response =
                handler.handleConflict(
                        new InvalidCampaignStatusException(
                                "Invalid campaign transition"
                        ),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody().status()).isEqualTo(409);
    }

    @Test
    void shouldReturn400WhenRequestDataIsInvalid() {
        ResponseEntity<ApiResponse> response =
                handler.handleBadRequest(
                        new IllegalArgumentException("Invalid status"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message())
                .isEqualTo("Invalid status");
    }

    @Test
    void shouldReturn500WithoutExposingInternalErrorDetails() {
        ResponseEntity<ApiResponse> response =
                handler.handleGeneral(
                        new RuntimeException("Database password leaked"),
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message())
                .isEqualTo("An unexpected error occurred");
    }

    @Test
    void shouldNotExposeEmailInConflictMessage() {
        EmailAlreadyExistsException exception =
                new EmailAlreadyExistsException("private@example.com");

        ResponseEntity<ApiResponse> response =
                handler.handleConflict(exception, request);

        assertThat(response.getBody().message())
                .doesNotContain("private@example.com");
    }
}