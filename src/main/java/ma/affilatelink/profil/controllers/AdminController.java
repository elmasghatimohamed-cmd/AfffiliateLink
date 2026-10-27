
package ma.affilatelink.profil.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.affilatelink.profil.services.AdminService;
import ma.affilatelink.profil.dto.ChangeStatusRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/profiles")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request
    ) {
        adminService.changeStatus(id, request.status());
        return ResponseEntity.noContent().build();
    }
}
