package ingprompt.patricia.parches.infrastructure.persistence.repository;

import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import ingprompt.patricia.parches.domain.model.Parche;
import ingprompt.patricia.parches.infrastructure.persistence.repository.mapper.ParcheMapper;
import ingprompt.patricia.parches.infrastructure.persistence.postgre.ParcheRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ParcheRepositoryAdapter implements ParcheRepositoryOutPort {
    private final ParcheRepository postgreRepository;

    @Override
    public void save(Parche parche) {
        postgreRepository.save(ParcheMapper.toEntity(parche));
    }

    @Override
    public void delete(Parche parche) {
        postgreRepository.deleteById(parche.getParcheId());
    }

    @Override
    public Optional<Parche> findById(UUID parcheId) {
        return postgreRepository.findById(parcheId).map(ParcheMapper::toDomain);
    }
}
