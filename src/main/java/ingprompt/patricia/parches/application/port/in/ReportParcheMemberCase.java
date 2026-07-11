package ingprompt.patricia.parches.application.port.in;

import ingprompt.patricia.parches.domain.enums.ReportType;
import ingprompt.patricia.parches.domain.model.ParcheReportMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReportParcheMemberCase {
    ParcheReportMember reportMember(UUID parcheId, UUID creatorId, UUID reportedId, ReportType reportType, String description);
    ParcheReportMember findById(UUID parcheId, UUID reportId, UUID requesterId, String roles);
    Page<ParcheReportMember> findByParcheId(UUID parcheId, UUID requesterId, String roles, Pageable pageable);
}
