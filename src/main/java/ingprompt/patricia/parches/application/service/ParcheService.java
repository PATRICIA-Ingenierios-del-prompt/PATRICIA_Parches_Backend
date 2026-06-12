package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.port.in.ManageParcheCase;
import ingprompt.patricia.parches.application.port.in.ParcheProvisioningCase;
import ingprompt.patricia.parches.application.port.out.ParcheEventPublisherOut;
import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.exception.NotParcheOwnerException;
import ingprompt.patricia.parches.domain.exception.ParcheNotFoundException;
import ingprompt.patricia.parches.domain.model.Parche;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ParcheService implements ManageParcheCase, ParcheProvisioningCase {
    private final ParcheRepositoryOutPort parcheRepository;
    private final ParcheEventPublisherOut eventPublisher;

    @Override
    @Transactional
    public Parche createParche(String name, ParcheCategory category, int num, UUID ownerId, String description, Visibility visibility) {
        Parche parche = new Parche(UUID.randomUUID(), name, category, num, ownerId, description, visibility);
        parcheRepository.save(parche);
        eventPublisher.publishParcheWasCreated(parche.getParcheId(), parche.getOwnerId());
        return parche;
    }

    @Override
    @Transactional
    public void deleteParche(UUID parcheId, UUID ownerId) {
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        if (!parche.isOwnedBy(ownerId)) {
            throw new NotParcheOwnerException(parcheId, ownerId);
        }
        parcheRepository.delete(parche);
    }

    @Override
    @Transactional
    public void assignCommunicationChannels(UUID parcheId, UUID chatId, UUID voiceId) {
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        parche.assignCommunication(chatId, voiceId);
        parcheRepository.save(parche);
    }

    @Override
    @Transactional
    public void assignCollaborationTools(UUID parcheId, UUID parquesId, UUID canvasId) {
        Parche parche = parcheRepository.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        parche.assignCollabs(parquesId, canvasId);
        parcheRepository.save(parche);
    }
}
