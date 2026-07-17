package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.application.port.in.ReportParcheMemberCase;
import ingprompt.patricia.parches.domain.model.ParcheReportMember;
import ingprompt.patricia.parches.infrastructure.web.dto.request.CreateReportRequest;
import ingprompt.patricia.parches.infrastructure.web.dto.response.ReportResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/parches/{parcheId}/reports")
@AllArgsConstructor
public class ReportController {

    private final ReportParcheMemberCase reportCase;

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@PathVariable UUID parcheId, @RequestBody CreateReportRequest request, @RequestHeader("X-User-Id") UUID creatorId) {
        ParcheReportMember report = reportCase.reportMember(parcheId, creatorId, request.getReportedId(), request.getReportType(), request.getDescription(), request.getReportedUserName(), request.getParcheName());
        return ResponseEntity.ok(ReportResponse.from(report));
    }

    @GetMapping
    public ResponseEntity<Page<ReportResponse>> listReports(@PathVariable UUID parcheId, @RequestHeader("X-User-Id") UUID requesterId, @RequestHeader("X-User-Roles") String roles, Pageable pageable) {
        Page<ReportResponse> page = reportCase.findByParcheId(parcheId, requesterId, roles, pageable).map(ReportResponse::from);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable UUID parcheId, @PathVariable UUID reportId, @RequestHeader("X-User-Id") UUID requesterId, @RequestHeader("X-User-Roles") String roles) {
        ParcheReportMember report = reportCase.findById(parcheId, reportId, requesterId, roles);
        return ResponseEntity.ok(ReportResponse.from(report));
    }
}
