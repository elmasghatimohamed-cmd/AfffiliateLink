package ma.affilatelink.profil.controllers;

import tools.jackson.databind.ObjectMapper;
import ma.affilatelink.common.exceptions.EmailAlreadyExistsException;
import ma.affilatelink.profil.dto.AdvertiserRequest;
import ma.affilatelink.profil.dto.AdvertiserResponse;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.services.AdvertiserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdvertiserController.class)
class AdvertiserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdvertiserService advertiserService;

    @Test
    void shouldCreateAdvertiserAndReturn201() throws Exception {
        AdvertiserRequest request = createAdvertiserRequest();

        when(advertiserService.create(any(AdvertiserRequest.class)))
                .thenReturn(createAdvertiserResponse());

        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("advertiser@test.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.companyName").value("First Company"))
                .andExpect(jsonPath("$.sector").value("Retail"))
                .andExpect(jsonPath("$.website")
                        .value("https://first-company.com"));

        verify(advertiserService).create(any(AdvertiserRequest.class));
    }

    @Test
    void shouldReturn400WhenAdvertiserRequestIsInvalid() throws Exception {
        String invalidRequest = """
                {
                    "email": "advertiser@test.com",
                    "sector": "Retail",
                    "website": "https://first-company.com"
                }
                """;

        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("companyName")))
                .andExpect(jsonPath("$.path").value("/api/advertisers"));
    }

    @Test
    void shouldReturnAllAdvertisers() throws Exception {
        when(advertiserService.findAll())
                .thenReturn(List.of(createAdvertiserResponse()));

        mockMvc.perform(get("/api/advertisers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email")
                        .value("advertiser@test.com"))
                .andExpect(jsonPath("$[0].companyName")
                        .value("First Company"))
                .andExpect(jsonPath("$[0].sector").value("Retail"));

        verify(advertiserService).findAll();
    }

    @Test
    void shouldReturnAdvertiserById() throws Exception {
        when(advertiserService.findById(1L))
                .thenReturn(createAdvertiserResponse());

        mockMvc.perform(get("/api/advertisers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("advertiser@test.com"))
                .andExpect(jsonPath("$.companyName")
                        .value("First Company"))
                .andExpect(jsonPath("$.sector").value("Retail"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(advertiserService).findById(1L);
    }

    @Test
    void shouldReturn404WhenAdvertiserDoesNotExist() throws Exception {
        when(advertiserService.findById(99L))
                .thenThrow(new NoSuchElementException(
                        "Advertiser not found with id: 99"
                ));

        mockMvc.perform(get("/api/advertisers/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Advertiser not found with id: 99"))
                .andExpect(jsonPath("$.path")
                        .value("/api/advertisers/99"));

        verify(advertiserService).findById(99L);
    }

    @Test
    void shouldReturn409WhenAdvertiserEmailAlreadyExists() throws Exception {
        AdvertiserRequest request = createAdvertiserRequest();

        when(advertiserService.create(any(AdvertiserRequest.class)))
                .thenThrow(new EmailAlreadyExistsException(request.email()));

        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/advertisers"));

        verify(advertiserService).create(any(AdvertiserRequest.class));
    }


    private AdvertiserRequest createAdvertiserRequest() {
        return new AdvertiserRequest(
                "advertiser@test.com",
                "First Company",
                "Retail",
                "https://first-company.com"
        );
    }

    private AdvertiserResponse createAdvertiserResponse() {
        return new AdvertiserResponse(
                1L,
                "advertiser@test.com",
                ProfileStatus.PENDING,
                LocalDateTime.of(2026, 11, 9, 10, 0),
                "First Company",
                "Retail",
                "https://first-company.com"
        );
    }
}
