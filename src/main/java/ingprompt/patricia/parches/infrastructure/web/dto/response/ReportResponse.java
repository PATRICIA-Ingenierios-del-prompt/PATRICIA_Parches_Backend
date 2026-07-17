package ingprompt.patricia.parches.infrastructure.web.dto.response;

import ingprompt.patricia.parches.domain.enums.ReportStatus;
import ingprompt.patricia.parches.domain.enums.ReportType;
import ingprompt.patricia.parches.domain.model.ParcheReportMember;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ReportResponse {
    private UUID reportId;
    private UUID parcheId;
    private UUID creatorId;
    private UUID reportedId;
    private ReportType reportType;
    private String description;
    private String reportedUserName;
    private String parcheName;
    private ReportStatus status;
    private Instant createdAt;
    private Instant resolvedAt;

    public static ReportResponse from(ParcheReportMember report) {
        return new ReportResponse(
                report.getReportId(),
                report.getParcheId(),
                report.getCreatorId(),
                report.getReportedId(),
                report.getReportType(),
                report.getDescription(),
                report.getReportedUserName(),
                report.getParcheName(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }
}
