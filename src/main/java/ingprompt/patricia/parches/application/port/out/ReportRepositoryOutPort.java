package ingprompt.patricia.parches.application.port.out;

import ingprompt.patricia.parches.domain.enums.ReportStatus;
import ingprompt.patricia.parches.domain.model.ParcheReportMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReportRepositoryOutPort {
    void save(ParcheReportMember report);
    Optional<ParcheReportMember> findById(UUID reportId);
    Page<ParcheReportMember> findByParcheId(UUID parcheId, Pageable pageable);
    Page<ParcheReportMember> findByStatus(ReportStatus status, Pageable pageable);
    Page<ParcheReportMember> findAll(Pageable pageable);
}
