package ma.affilatelink.offer.controllers;

import ma.affilatelink.offer.dto.CampaignResponse;
import ma.affilatelink.offer.dto.CreateOfferRequest;
import ma.affilatelink.offer.dto.OfferResponse;
import ma.affilatelink.offer.dto.UpdateOfferRequest;
import ma.affilatelink.offer.services.OfferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.mockito.ArgumentCaptor;

@WebMvcTest(OfferController.class)
class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OfferService offerService;

    @Test
    void shouldCreateOfferAndReturn201() throws Exception {
        CreateOfferRequest request = createOfferRequest();

        when(offerService.createOffer(any(CreateOfferRequest.class)))
                .thenReturn(createOfferResponse());

        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.advertiserId").value(10))
                .andExpect(jsonPath("$.title").value("Beauty Essentials"))
                .andExpect(jsonPath("$.description")
                        .value("Beauty products for everyday use"))
                .andExpect(jsonPath("$.landingUrl")
                        .value("https://example.com/beauty"))
                .andExpect(jsonPath("$.niche").value("BEAUTY"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.campaigns").isArray());

        verify(offerService).createOffer(any(CreateOfferRequest.class));
    }

    @Test
    void shouldReturn400WhenCreateOfferRequestIsInvalid() throws Exception {
        String invalidRequest = """
                {
                    "advertiserId": -1,
                    "title": "",
                    "description": "",
                    "landingUrl": "not-a-url",
                    "niche": ""
                }
                """;

        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/offers"));
    }

    @Test
    void shouldReturn400WhenRequestContainsMalformedJson() throws Exception {
        String malformedJson = """
                {
                    "advertiserId": 10,
                    "title": "Beauty Essentials",
                    "description":
                """;

        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid request data"))
                .andExpect(jsonPath("$.path").value("/api/offers"));
    }

    @Test
    void shouldUpdateOfferAndReturn200() throws Exception {
        UpdateOfferRequest request = createUpdateOfferRequest();

        when(offerService.updateOffer(eq(1L), any(UpdateOfferRequest.class)))
                .thenReturn(createOfferResponse());

        mockMvc.perform(put("/api/offers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Beauty Essentials"))
                .andExpect(jsonPath("$.niche").value("BEAUTY"));

        verify(offerService).updateOffer(
                eq(1L),
                any(UpdateOfferRequest.class)
        );
    }

    @Test
    void shouldReturnOfferById() throws Exception {
        when(offerService.getOfferById(1L))
                .thenReturn(createOfferResponse());

        mockMvc.perform(get("/api/offers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.advertiserId").value(10))
                .andExpect(jsonPath("$.title").value("Beauty Essentials"))
                .andExpect(jsonPath("$.niche").value("BEAUTY"));

        verify(offerService).getOfferById(1L);
    }

    @Test
    void shouldReturn404WhenOfferDoesNotExist() throws Exception {
        when(offerService.getOfferById(99L))
                .thenThrow(new NoSuchElementException(
                        "Offer not found with id: 99"
                ));

        mockMvc.perform(get("/api/offers/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Offer not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/api/offers/99"));

        verify(offerService).getOfferById(99L);
    }

    @Test
    void shouldSearchOffersUsingProvidedFiltersAndPagination()
            throws Exception {

        OfferResponse response = createOfferResponse();

        Pageable requestedPageable = PageRequest.of(
                1,
                2,
                Sort.by(Sort.Direction.ASC, "title")
        );

        Page<OfferResponse> result = new PageImpl<>(
                List.of(response),
                requestedPageable,
                5
        );

        when(offerService.searchOffers(
                eq("BEAUTY"),
                eq(10L),
                eq("Java"),
                eq("beauty"),
                any(Pageable.class)
        )).thenReturn(result);

        mockMvc.perform(get("/api/offers")
                        .param("niche", "BEAUTY")
                        .param("advertiserId", "10")
                        .param("tag", "Java")
                        .param("keyword", "beauty")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Beauty Essentials"))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2));

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(offerService).searchOffers(
                eq("BEAUTY"),
                eq(10L),
                eq("Java"),
                eq("beauty"),
                pageableCaptor.capture()
        );

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(1, capturedPageable.getPageNumber());
        assertEquals(2, capturedPageable.getPageSize());
        assertEquals(
                Sort.Direction.ASC,
                capturedPageable.getSort().getOrderFor("title").getDirection()
        );
    }

    @Test
    void shouldApplyDefaultPaginationAndSorting() throws Exception {
        when(offerService.searchOffers(
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(Page.empty(PageRequest.of(0, 20)));

        mockMvc.perform(get("/api/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(offerService).searchOffers(
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                pageableCaptor.capture()
        );

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(20, capturedPageable.getPageSize());
        assertEquals(
                Sort.Direction.DESC,
                capturedPageable.getSort()
                        .getOrderFor("createdAt")
                        .getDirection()
        );
    }

    @Test
    void shouldReturnEmptyPageWhenNoOffersMatchSearch() throws Exception {
        when(offerService.searchOffers(
                eq("TECHNOLOGY"),
                eq(null),
                eq(null),
                eq("unknown"),
                any(Pageable.class)
        )).thenReturn(Page.empty(PageRequest.of(0, 20)));

        mockMvc.perform(get("/api/offers")
                        .param("niche", "TECHNOLOGY")
                        .param("keyword", "unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(offerService).searchOffers(
                eq("TECHNOLOGY"),
                eq(null),
                eq(null),
                eq("unknown"),
                any(Pageable.class)
        );
    }

    @Test
    void shouldReturn404WhenUpdatingOfferThatDoesNotExist()
            throws Exception {

        UpdateOfferRequest request = createUpdateOfferRequest();

        when(offerService.updateOffer(
                eq(99L),
                any(UpdateOfferRequest.class)
        )).thenThrow(new NoSuchElementException(
                "Offer not found with id: 99"
        ));

        mockMvc.perform(put("/api/offers/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Offer not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/api/offers/99"));

        verify(offerService).updateOffer(
                eq(99L),
                any(UpdateOfferRequest.class)
        );
    }

    private CreateOfferRequest createOfferRequest() {
        return new CreateOfferRequest(
                10L,
                "Beauty Essentials",
                "Beauty products for everyday use",
                "https://example.com/beauty",
                "BEAUTY",
                Set.of("Skincare", "Beauty")
        );
    }

    private UpdateOfferRequest createUpdateOfferRequest() {
        return new UpdateOfferRequest(
                "Beauty Essentials",
                "Beauty products for everyday use",
                "https://example.com/beauty",
                "BEAUTY",
                Set.of("Skincare", "Beauty")
        );
    }

    private OfferResponse createOfferResponse() {
        return new OfferResponse(
                1L,
                10L,
                "Beauty Essentials",
                "Beauty products for everyday use",
                "https://example.com/beauty",
                "BEAUTY",
                LocalDateTime.of(2026, 11, 9, 10, 0),
                Set.of("Skincare", "Beauty"),
                List.<CampaignResponse>of()
        );
    }
}
