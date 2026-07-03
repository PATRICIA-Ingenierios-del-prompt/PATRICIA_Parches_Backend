package ingprompt.patricia.parches.infrastructure.persistence.postgre;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.infrastructure.persistence.entity.ParcheEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ParcheRepository extends JpaRepository<ParcheEntity, UUID> {
    Page<ParcheEntity> findByCategory(ParcheCategory category, Pageable pageable);
    Page<ParcheEntity> findByVisibility(Visibility visibility, Pageable pageable);
    Page<ParcheEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query(value = "SELECT p FROM ParcheEntity p WHERE SIZE(p.members) < p.maxCapacity",
            countQuery = "SELECT COUNT(p) FROM ParcheEntity p WHERE SIZE(p.members) < p.maxCapacity")
    Page<ParcheEntity> findWithOpenSpots(Pageable pageable);

    // Parches the given user belongs to (the owner is always a member).
    @Query(value = "SELECT p FROM ParcheEntity p WHERE :userId MEMBER OF p.members",
            countQuery = "SELECT COUNT(p) FROM ParcheEntity p WHERE :userId MEMBER OF p.members")
    Page<ParcheEntity> findByMember(@Param("userId") UUID userId, Pageable pageable);
}
