package ingprompt.patricia.parches.infrastructure.persistence.entity;

import ingprompt.patricia.parches.domain.enums.ReportType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "parche_reports")
public class ReportEntity {
    @Id
    private UUID reportId;

    @Column(nullable = false)
    private UUID parcheId;

    @Column(nullable = false)
    private UUID creatorId;

    @Column(nullable = false)
    private UUID reportedId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Instant createdAt;
}
