package ingprompt.patricia.parches.infrastructure.web;

import ingprompt.patricia.parches.application.port.in.ReportParcheMemberCase;
import ingprompt.patricia.parches.domain.enums.ReportStatus;
import ingprompt.patricia.parches.infrastructure.web.dto.response.ReportResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/parches/admin/reports")
@AllArgsConstructor
public class AdminReportController {

    private final ReportParcheMemberCase reportCase;

    @GetMapping
    public ResponseEntity<Page<ReportResponse>> listAllReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestHeader("X-User-Roles") String roles,
            Pageable pageable) {
        return ResponseEntity.ok(reportCase.findAll(status, roles, pageable).map(ReportResponse::from));
    }

    @PostMapping("/{reportId}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable UUID reportId,
            @RequestHeader("X-User-Roles") String roles) {
        return ResponseEntity.ok(ReportResponse.from(reportCase.resolveReport(reportId, roles)));
    }
}
