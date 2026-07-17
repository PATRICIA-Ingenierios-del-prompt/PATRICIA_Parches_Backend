package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.port.in.ParcheAdminCase;
import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import org.springframework.stereotype.Service;

@Service
public class ParcheAdminService implements ParcheAdminCase {

    private final ParcheRepositoryOutPort parcheRepository;

    public ParcheAdminService(ParcheRepositoryOutPort parcheRepository) {
        this.parcheRepository = parcheRepository;
    }

    @Override
    public long countParches() {
        return parcheRepository.count();
    }
}
