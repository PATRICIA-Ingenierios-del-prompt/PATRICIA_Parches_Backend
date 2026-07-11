package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.application.port.in.ManageMemberParcheCase;
import ingprompt.patricia.parches.application.port.in.ManageParcheCase;
import ingprompt.patricia.parches.application.port.in.ParcheQueryCase;
import ingprompt.patricia.parches.application.port.in.SpecialQueriesFilterCases;
import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.model.Parche;
import ingprompt.patricia.parches.infrastructure.web.dto.request.CreateParcheRequest;
import ingprompt.patricia.parches.infrastructure.web.dto.response.CreateParcheResponse;
import ingprompt.patricia.parches.infrastructure.web.dto.response.ParcheResponse;
import ingprompt.patricia.parches.infrastructure.web.dto.response.ParcheSummaryResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/parches")
@AllArgsConstructor
public class ParcheController {
    private final ManageParcheCase serviceManageParche;
    private final ManageMemberParcheCase serviceManageMemberParcheCase;
    private final ParcheQueryCase serviceParcheQuery;
    private final SpecialQueriesFilterCases serviceFilter;

    @PostMapping
    public ResponseEntity<CreateParcheResponse> createParche(@RequestBody CreateParcheRequest request, @RequestHeader("X-User-Id") UUID ownerId) {
        Parche newParche = serviceManageParche.createParche(request.getName(), request.getCategory(), request.getMaxCapacity(), ownerId, request.getDescription(), request.getVisibility(), request.getPictureUrl());
        CreateParcheResponse response = new CreateParcheResponse(newParche.getParcheId(), newParche.getName(), newParche.getDescription(), newParche.getVisibility(), newParche.getStatus(), newParche.getPictureUrl());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{parcheId}")
    public ResponseEntity<Void> deleteParche(@PathVariable UUID parcheId, @RequestHeader("X-User-Id") UUID ownerId) {
        serviceManageParche.deleteParche(parcheId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{parcheId}/join")
    public ResponseEntity<Void> joinParche(@PathVariable UUID parcheId, @RequestHeader("X-User-Id") UUID userId) {
        serviceManageMemberParcheCase.joinPublicParche(parcheId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{parcheId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable UUID parcheId, @PathVariable UUID memberId, @RequestHeader("X-User-Id") UUID requesterId) {
        serviceManageMemberParcheCase.removeMemberFromParche(parcheId, memberId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{parcheId}")
    public ResponseEntity<ParcheResponse> getParche(@PathVariable UUID parcheId) {
        return ResponseEntity.ok(ParcheResponse.from(serviceParcheQuery.getParcheById(parcheId)));
    }
    @GetMapping("/{parcheId}/events")
    public ResponseEntity<Set<UUID>> getParcheEvents(@PathVariable UUID parcheId) {
        return ResponseEntity.ok(serviceParcheQuery.getEventsOfParche(parcheId));
    }

    @GetMapping("/{parcheId}/members")
    public ResponseEntity<Set<UUID>> getParcheMembers(@PathVariable UUID parcheId) {
        return ResponseEntity.ok(serviceParcheQuery.getMembersOfParche(parcheId));
    }

    @GetMapping("/category")
    public ResponseEntity<Page<ParcheSummaryResponse>> filterByCategory(@RequestParam ParcheCategory category, @RequestHeader("X-User-Id") UUID userId, Pageable pageable) {
        return ResponseEntity.ok(serviceFilter.filterByCategory(category, pageable).map(ParcheSummaryResponse::from));
    }

    @GetMapping("/visibility")
    public ResponseEntity<Page<ParcheSummaryResponse>> filterByVisibility(@RequestParam Visibility visibility, @RequestHeader("X-User-Id") UUID userId, Pageable pageable) {
        return ResponseEntity.ok(serviceFilter.filterByVisibility(visibility, pageable).map(ParcheSummaryResponse::from));
    }

    @GetMapping("/capacity")
    public ResponseEntity<Page<ParcheSummaryResponse>> filterByOpenSpots(@RequestHeader("X-User-Id") UUID userId, Pageable pageable) {
        return ResponseEntity.ok(serviceFilter.filterByOpenSpots(pageable).map(ParcheSummaryResponse::from));
    }

    @GetMapping("/name")
    public ResponseEntity<Page<ParcheSummaryResponse>> filterByName(@RequestParam String name, @RequestHeader("X-User-Id") UUID userId, Pageable pageable) {
        return ResponseEntity.ok(serviceFilter.findByName(name, pageable).map(ParcheSummaryResponse::from));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<ParcheSummaryResponse>> myParches(@RequestHeader("X-User-Id") UUID userId, Pageable pageable) {
        return ResponseEntity.ok(serviceFilter.findParchesForMember(userId, pageable).map(ParcheSummaryResponse::from));
    }
}
