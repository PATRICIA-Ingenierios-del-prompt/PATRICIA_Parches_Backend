package ingprompt.patricia.parches.application.port.out;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.model.Parche;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ParcheRepositoryOutPort {
    void save(Parche parche);
    void delete(Parche parche);
    Optional<Parche> findById(UUID parcheId);
    long count();

    Page<Parche> findByCategory(ParcheCategory category, Pageable pageable);
    Page<Parche> findByVisibility(Visibility visibility, Pageable pageable);
    Page<Parche> findWithOpenSpots(Pageable pageable);
    Page<Parche> findByNameContaining(String name, Pageable pageable);
    Page<Parche> findByMember(UUID userId, Pageable pageable);
}
