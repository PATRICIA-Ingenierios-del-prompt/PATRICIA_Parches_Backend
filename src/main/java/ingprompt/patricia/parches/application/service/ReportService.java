package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.port.in.ReportParcheMemberCase;
import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import ingprompt.patricia.parches.application.port.out.ReportRepositoryOutPort;
import ingprompt.patricia.parches.domain.enums.ReportStatus;
import ingprompt.patricia.parches.domain.enums.ReportType;
import ingprompt.patricia.parches.domain.exception.InvalidReportException;
import ingprompt.patricia.parches.domain.exception.ParcheNotFoundException;
import ingprompt.patricia.parches.domain.exception.UnauthorizedReportAccessException;
import ingprompt.patricia.parches.domain.model.Parche;
import ingprompt.patricia.parches.domain.model.ParcheReportMember;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ReportService implements ReportParcheMemberCase {
    private static final Set<String> AUTHORIZED_ROLES = Set.of("ADMIN");

    private final ReportRepositoryOutPort reportRepository;
    private final ParcheRepositoryOutPort parcheRepository;

    @Override
    public ParcheReportMember reportMember(UUID parcheId, UUID creatorId, UUID reportedId, ReportType reportType, String description, String reportedUserName, String parcheName) {
        if (creatorId.equals(reportedId)) {
            throw new InvalidReportException("A user cannot report themselves");
        }
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        if (!parche.hasMember(creatorId)) {
            throw new InvalidReportException("Creator " + creatorId + " is not a member of parche " + parcheId);
        }
        if (!parche.hasMember(reportedId)) {
            throw new InvalidReportException("Reported user " + reportedId + " is not a member of parche " + parcheId);
        }

        String finalReportedUserName = reportedUserName != null && !reportedUserName.isBlank() ? reportedUserName : reportedId.toString();
        String finalParcheName = parcheName != null && !parcheName.isBlank() ? parcheName : parche.getName();

        ParcheReportMember report = ParcheReportMember.of(parcheId, creatorId, reportedId, reportType, description, finalReportedUserName, finalParcheName);
        reportRepository.save(report);
        log.info("Report {} created on parche {} by {} against {}", report.getReportId(), parcheId, creatorId, reportedId);
        return report;
    }

    @Override
    public ParcheReportMember findById(UUID parcheId, UUID reportId, UUID requesterId, String roles) {
        authorizeRead(parcheId, requesterId, roles);
        ParcheReportMember report = reportRepository.findById(reportId).orElseThrow(() -> new InvalidReportException("Report " + reportId + " not found"));
        if (!report.getParcheId().equals(parcheId)) {
            throw new InvalidReportException("Report " + reportId + " does not belong to parche " + parcheId);
        }
        return report;
    }

    @Override
    public Page<ParcheReportMember> findByParcheId(UUID parcheId, UUID requesterId, String roles, Pageable pageable) {
        authorizeRead(parcheId, requesterId, roles);
        return reportRepository.findByParcheId(parcheId, pageable);
    }

    @Override
    public Page<ParcheReportMember> findAll(ReportStatus status, String roles, Pageable pageable) {
        if (!hasAuthorizedRole(roles)) {
            throw new UnauthorizedReportAccessException(null, roles);
        }
        if (status != null) {
            return reportRepository.findByStatus(status, pageable);
        }
        return reportRepository.findAll(pageable);
    }

    @Override
    public ParcheReportMember resolveReport(UUID reportId, String roles) {
        if (!hasAuthorizedRole(roles)) {
            throw new UnauthorizedReportAccessException(null, roles);
        }
        ParcheReportMember report = reportRepository.findById(reportId)
                .orElseThrow(() -> new InvalidReportException("Report " + reportId + " not found"));
        report.resolve();
        reportRepository.save(report);
        log.info("Report {} resolved by admin", reportId);
        return report;
    }

    private void authorizeRead(UUID parcheId, UUID requesterId, String roles) {
        if (hasAuthorizedRole(roles)) {
            return;
        }
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        if (!parche.isOwnedBy(requesterId)) {
            throw new UnauthorizedReportAccessException(requesterId, roles);
        }
    }

    private boolean hasAuthorizedRole(String roles) {
        return roles != null && Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(AUTHORIZED_ROLES::contains);
    }
}
