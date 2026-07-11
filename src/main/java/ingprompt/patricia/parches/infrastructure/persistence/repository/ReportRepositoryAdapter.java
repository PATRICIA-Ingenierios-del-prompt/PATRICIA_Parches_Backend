package ingprompt.patricia.parches.infrastructure.persistence.repository;

import ingprompt.patricia.parches.application.port.out.ReportRepositoryOutPort;
import ingprompt.patricia.parches.domain.model.ParcheReportMember;
import ingprompt.patricia.parches.infrastructure.persistence.postgre.ReportRepository;
import ingprompt.patricia.parches.infrastructure.persistence.repository.mapper.ReportMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ReportRepositoryAdapter implements ReportRepositoryOutPort {
    private final ReportRepository reportRepository;

    @Override
    public void save(ParcheReportMember report) {
        reportRepository.save(ReportMapper.toEntity(report));
    }

    @Override
    public Optional<ParcheReportMember> findById(UUID reportId) {
        return reportRepository.findById(reportId).map(ReportMapper::toDomain);
    }

    @Override
    public Page<ParcheReportMember> findByParcheId(UUID parcheId, Pageable pageable) {
        return reportRepository.findByParcheId(parcheId, pageable).map(ReportMapper::toDomain);
    }
}
