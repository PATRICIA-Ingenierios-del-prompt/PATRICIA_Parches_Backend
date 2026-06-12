package ingprompt.patricia.parches.infrastructure.persistence.postgre;

import ingprompt.patricia.parches.infrastructure.persistence.entity.ParcheEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParcheRepository extends JpaRepository<ParcheEntity, UUID> {
}
