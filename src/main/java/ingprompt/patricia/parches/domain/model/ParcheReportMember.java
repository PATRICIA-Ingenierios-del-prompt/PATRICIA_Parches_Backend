package ingprompt.patricia.parches.domain.model;

import ingprompt.patricia.parches.domain.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ParcheReportMember {
    private UUID reportId;
    private UUID parcheId;
    private UUID creatorId;
    private UUID reportedId;
    private ReportType reportType;
    private String description;
    private Instant createdAt;


    public static ParcheReportMember of(UUID parcheId, UUID creatorId, UUID reportedId, ReportType reportType, String description) {
        return new ParcheReportMember(UUID.randomUUID(), parcheId, creatorId, reportedId, reportType, description, Instant.now());
    }
}
