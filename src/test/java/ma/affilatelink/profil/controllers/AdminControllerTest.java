package ma.affilatelink.profil.controllers;

import tools.jackson.databind.ObjectMapper;
import ma.affilatelink.common.exceptions.InvalidProfileStatusException;
import ma.affilatelink.profil.dto.ChangeStatusRequest;
import ma.affilatelink.profil.enums.ProfileStatus;
import ma.affilatelink.profil.services.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @Test
    void shouldChangeProfileStatusAndReturn204() throws Exception {
        // Arrange
        Long profileId = 1L;

        ChangeStatusRequest request =
                new ChangeStatusRequest(ProfileStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(adminService).changeStatus(
                profileId,
                ProfileStatus.ACTIVE
        );
    }

    @Test
    void shouldReturn400WhenStatusIsMissing() throws Exception {
        // Arrange
        String invalidRequest = """
                {
                    "status": null
                }
                """;

        // Act & Assert
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("status: Status is required"))
                .andExpect(jsonPath("$.path")
                        .value("/api/admin/profiles/1/status"));
    }

    @Test
    void shouldReturn404WhenProfileDoesNotExist() throws Exception {
        // Arrange
        Long profileId = 99L;

        ChangeStatusRequest request =
                new ChangeStatusRequest(ProfileStatus.ACTIVE);

        doThrow(new NoSuchElementException(
                "Profile not found with id: " + profileId
        )).when(adminService).changeStatus(
                profileId,
                ProfileStatus.ACTIVE
        );

        // Act & Assert
        mockMvc.perform(patch(
                        "/api/admin/profiles/{id}/status",
                        profileId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Profile not found with id: 99"))
                .andExpect(jsonPath("$.path")
                        .value("/api/admin/profiles/99/status"));

        verify(adminService).changeStatus(
                profileId,
                ProfileStatus.ACTIVE
        );
    }

    @Test
    void shouldReturn409WhenProfileStatusTransitionIsInvalid()
            throws Exception {
        // Arrange
        Long profileId = 1L;

        ChangeStatusRequest request =
                new ChangeStatusRequest(ProfileStatus.ACTIVE);

        doThrow(new InvalidProfileStatusException(
                "Profile is already ACTIVE"
        )).when(adminService).changeStatus(
                profileId,
                ProfileStatus.ACTIVE
        );

        // Act & Assert
        mockMvc.perform(patch(
                        "/api/admin/profiles/{id}/status",
                        profileId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Profile is already ACTIVE"))
                .andExpect(jsonPath("$.path")
                        .value("/api/admin/profiles/1/status"));

        verify(adminService).changeStatus(
                profileId,
                ProfileStatus.ACTIVE
        );
    }
}
