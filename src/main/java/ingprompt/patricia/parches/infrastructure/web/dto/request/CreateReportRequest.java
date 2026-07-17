package ingprompt.patricia.parches.infrastructure.web.dto.request;

import ingprompt.patricia.parches.domain.enums.ReportType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateReportRequest {
    private UUID reportedId;
    private ReportType reportType;
    private String description;
    private String reportedUserName;
    private String parcheName;
}
