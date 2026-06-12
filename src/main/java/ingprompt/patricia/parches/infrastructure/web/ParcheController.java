package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.application.port.in.ManageParcheCase;
import ingprompt.patricia.parches.domain.model.Parche;
import ingprompt.patricia.parches.infrastructure.web.dto.request.CreateParcheRequest;
import ingprompt.patricia.parches.infrastructure.web.dto.response.CreateParcheResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/parches")
@AllArgsConstructor
public class ParcheController {
    private final ManageParcheCase serviceManageParche;

    @PostMapping
    public ResponseEntity<CreateParcheResponse> createParche(@RequestBody CreateParcheRequest request, @RequestHeader("X-User-Id") UUID ownerId) {
        Parche newParche = serviceManageParche.createParche(request.getName(), request.getCategory(), request.getMaxCapacity(), ownerId, request.getDescription(), request.getVisibility());
        CreateParcheResponse response = new CreateParcheResponse(newParche.getParcheId(), newParche.getName(), newParche.getDescription(), newParche.getVisibility(), newParche.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{parcheId}")
    public ResponseEntity<Void> deleteParche(@PathVariable UUID parcheId, @RequestHeader("X-User-Id") UUID ownerId) {
        serviceManageParche.deleteParche(parcheId, ownerId);
        return ResponseEntity.noContent().build();
    }
}
