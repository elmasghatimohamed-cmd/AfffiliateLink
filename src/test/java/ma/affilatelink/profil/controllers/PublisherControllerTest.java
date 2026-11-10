package ma.affilatelink.profil.controllers;

import ma.affilatelink.common.exceptions.EmailAlreadyExistsException;
import ma.affilatelink.profil.dto.PublisherRequest;
import ma.affilatelink.profil.dto.PublisherResponse;
import ma.affilatelink.profil.enums.Niche;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.services.PublisherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublisherController.class)
class PublisherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PublisherService publisherService;

    @Test
    void shouldCreatePublisherAndReturn201() throws Exception {
        PublisherRequest request = createPublisherRequest();

        when(publisherService.create(any(PublisherRequest.class)))
                .thenReturn(createPublisherResponse());

        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("publisher@test.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.niche").value("BEAUTY"))
                .andExpect(jsonPath("$.bio")
                        .value("Content creator focused on beauty"))
                .andExpect(jsonPath("$.country").value("Morocco"));

        verify(publisherService).create(any(PublisherRequest.class));
    }

    @Test
    void shouldReturn400WhenPublisherRequestIsInvalid() throws Exception {
        String invalidRequest = """
                {
                    "email": "publisher@test.com",
                    "lastname": "Doe",
                    "niche": "BEAUTY",
                    "bio": "Content creator focused on beauty",
                    "country": "Morocco"
                }
                """;

        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("firstname: First name is required"))
                .andExpect(jsonPath("$.path").value("/api/publishers"));
    }

    @Test
    void shouldReturnAllPublishers() throws Exception {
        PublisherResponse response = createPublisherResponse();

        when(publisherService.findAll())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email")
                        .value("publisher@test.com"))
                .andExpect(jsonPath("$[0].firstname").value("John"))
                .andExpect(jsonPath("$[0].niche").value("BEAUTY"));

        verify(publisherService).findAll();
    }

    @Test
    void shouldReturnPublisherById() throws Exception {
        PublisherResponse response = createPublisherResponse();

        when(publisherService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/publishers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email")
                        .value("publisher@test.com"))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(publisherService).findById(1L);
    }

    @Test
    void shouldReturn404WhenPublisherDoesNotExist() throws Exception {
        when(publisherService.findById(99L))
                .thenThrow(new NoSuchElementException(
                        "Publisher not found with id: 99"
                ));

        mockMvc.perform(get("/api/publishers/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Publisher not found with id: 99"))
                .andExpect(jsonPath("$.path")
                        .value("/api/publishers/99"));

        verify(publisherService).findById(99L);
    }

    @Test
    void shouldReturn409WhenPublisherEmailAlreadyExists() throws Exception {
        PublisherRequest request = createPublisherRequest();

        when(publisherService.create(any(PublisherRequest.class)))
                .thenThrow(new EmailAlreadyExistsException(request.email()));

        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/publishers"));

        verify(publisherService).create(any(PublisherRequest.class));
    }


    private PublisherRequest createPublisherRequest() {
        return new PublisherRequest(
                "publisher@test.com",
                "John",
                "Doe",
                Niche.BEAUTY,
                "Content creator focused on beauty",
                "Morocco"
        );
    }

    private PublisherResponse createPublisherResponse() {
        return new PublisherResponse(
                1L,
                "publisher@test.com",
                ProfileStatus.PENDING,
                LocalDateTime.of(2026, 11, 9, 10, 0),
                "John",
                "Doe",
                Niche.BEAUTY,
                "Content creator focused on beauty",
                "Morocco"
        );
    }
}
