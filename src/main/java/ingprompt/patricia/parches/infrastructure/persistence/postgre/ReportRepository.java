package ingprompt.patricia.parches.infrastructure.persistence.postgre;

import ingprompt.patricia.parches.domain.enums.ReportStatus;
import ingprompt.patricia.parches.infrastructure.persistence.entity.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {
    Page<ReportEntity> findByParcheId(UUID parcheId, Pageable pageable);

    Page<ReportEntity> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    Page<ReportEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
