package ingprompt.patricia.parches.application.port.in;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.model.Parche;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SpecialQueriesFilterCases {
    Page<Parche> filterByCategory(ParcheCategory category, Pageable pageable);
    Page<Parche> filterByVisibility(Visibility visibility, Pageable pageable);
    Page<Parche> filterByOpenSpots(Pageable pageable);
    Page<Parche> findByName(String name, Pageable pageable);
    Page<Parche> findParchesForMember(UUID userId, Pageable pageable);
}
