package ma.affilatelink.offer.controllers;

import ma.affilatelink.offer.dto.CampaignCreateRequest;
import ma.affilatelink.offer.dto.CampaignResponse;
import ma.affilatelink.offer.dto.UpdateCampaignStatusRequest;
import ma.affilatelink.offer.enums.CampaignStatus;
import ma.affilatelink.offer.enums.PayoutModel;
import ma.affilatelink.offer.services.CampaignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CampaignService campaignService;

    @Test
    void shouldCreateCampaignAndReturn201() throws Exception {
        CampaignCreateRequest request = createCampaignRequest();

        when(campaignService.createCampaign(
                eq(1L),
                eq(request)
        )).thenReturn(createCampaignResponse());

        mockMvc.perform(post("/api/offres/{id}/campaigns", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Spring Promotion"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.offerId").value(1));

        verify(campaignService).createCampaign(eq(1L), eq(request));
    }

    @Test
    void shouldReturn400WhenCreateCampaignRequestIsInvalid()
            throws Exception {

        String invalidRequest = """
                {
                    "name": "",
                    "startDate": "2026-11-10",
                    "endDate": "2026-11-20",
                    "budget": 1000.00,
                    "payoutModel": "CPC",
                    "payoutValue": 5.00
                }
                """;

        mockMvc.perform(post("/api/offres/{id}/campaigns", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(campaignService);
    }

    @Test
    void shouldReturnAllCampaignsWhenStatusIsNotProvided()
            throws Exception {

        when(campaignService.getCampaigns(null))
                .thenReturn(List.of(createCampaignResponse()));

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Spring Promotion"));

        verify(campaignService).getCampaigns(null);
    }

    @Test
    void shouldReturnAllCampaignsWhenStatusIsBlank()
            throws Exception {

        when(campaignService.getCampaigns(null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/campaigns")
                        .param("status", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(campaignService).getCampaigns(null);
    }

    @Test
    void shouldFilterCampaignsUsingCaseInsensitiveStatus()
            throws Exception {

        when(campaignService.getCampaigns(CampaignStatus.ACTIVE))
                .thenReturn(List.of(createCampaignResponse()));

        mockMvc.perform(get("/api/campaigns")
                        .param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(campaignService).getCampaigns(CampaignStatus.ACTIVE);
    }

    @Test
    void shouldReturn400WhenCampaignStatusIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/campaigns")
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(campaignService);
    }

    @Test
    void shouldUpdateCampaignStatusAndReturn204()
            throws Exception {

        UpdateCampaignStatusRequest request =
                new UpdateCampaignStatusRequest(CampaignStatus.ACTIVE);

        mockMvc.perform(patch("/api/campaigns/{id}/status", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(campaignService).updateCampaignStatus(
                10L,
                CampaignStatus.ACTIVE
        );
    }

    @Test
    void shouldReturn400WhenCampaignStatusIsMissing()
            throws Exception {

        String invalidRequest = """
                {
                    "status": null
                }
                """;

        mockMvc.perform(patch("/api/campaigns/{id}/status", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(campaignService);
    }

    private CampaignCreateRequest createCampaignRequest() {
        return new CampaignCreateRequest(
                "Spring Promotion",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 20),
                new BigDecimal("1000.00"),
                PayoutModel.CPC,
                new BigDecimal("5.00")
        );
    }

    private CampaignResponse createCampaignResponse() {
        return new CampaignResponse(
                10L,
                "Spring Promotion",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 20),
                new BigDecimal("1000.00"),
                PayoutModel.CPC,
                new BigDecimal("5.00"),
                CampaignStatus.DRAFT,
                1L
        );
    }
}
