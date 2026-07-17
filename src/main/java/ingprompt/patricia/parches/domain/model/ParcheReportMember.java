package ingprompt.patricia.parches.domain.model;

import ingprompt.patricia.parches.domain.enums.ReportStatus;
import ingprompt.patricia.parches.domain.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcheReportMember {
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

    public static ParcheReportMember of(UUID parcheId, UUID creatorId, UUID reportedId, ReportType reportType, String description, String reportedUserName, String parcheName) {
        return new ParcheReportMember(UUID.randomUUID(), parcheId, creatorId, reportedId, reportType, description, reportedUserName, parcheName, ReportStatus.PENDING, Instant.now(), null);
    }

    public void resolve() {
        this.status = ReportStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }
}
