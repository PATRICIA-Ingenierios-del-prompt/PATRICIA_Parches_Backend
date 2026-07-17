package ingprompt.patricia.parches.infrastructure.persistence.repository.mapper;

import ingprompt.patricia.parches.domain.model.ParcheReportMember;
import ingprompt.patricia.parches.infrastructure.persistence.entity.ReportEntity;

public final class ReportMapper {

    private ReportMapper() {
    }

    public static ReportEntity toEntity(ParcheReportMember report) {
        ReportEntity entity = new ReportEntity();
        entity.setReportId(report.getReportId());
        entity.setParcheId(report.getParcheId());
        entity.setCreatorId(report.getCreatorId());
        entity.setReportedId(report.getReportedId());
        entity.setReportType(report.getReportType());
        entity.setDescription(report.getDescription());
        entity.setReportedUserName(report.getReportedUserName());
        entity.setParcheName(report.getParcheName());
        entity.setStatus(report.getStatus());
        entity.setCreatedAt(report.getCreatedAt());
        entity.setResolvedAt(report.getResolvedAt());
        return entity;
    }

    public static ParcheReportMember toDomain(ReportEntity entity) {
        return new ParcheReportMember(
                entity.getReportId(),
                entity.getParcheId(),
                entity.getCreatorId(),
                entity.getReportedId(),
                entity.getReportType(),
                entity.getDescription(),
                entity.getReportedUserName(),
                entity.getParcheName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getResolvedAt()
        );
    }
}
