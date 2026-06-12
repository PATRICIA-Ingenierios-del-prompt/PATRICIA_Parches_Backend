package ingprompt.patricia.parches.application.port.out;

import ingprompt.patricia.parches.domain.model.Parche;

import java.util.Optional;
import java.util.UUID;

public interface ParcheRepositoryOutPort {
    void save(Parche parche);

    void delete(Parche parche);

    Optional<Parche> findById(UUID parcheId);
}
