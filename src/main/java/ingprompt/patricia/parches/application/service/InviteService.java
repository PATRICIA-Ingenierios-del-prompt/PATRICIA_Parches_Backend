package ingprompt.patricia.parches.application.service;

import ingprompt.patricia.parches.application.port.in.InviteUserCase;
import ingprompt.patricia.parches.application.port.out.InviteRepositoryOutPort;
import ingprompt.patricia.parches.application.port.out.InviteTokenGeneratorPort;
import ingprompt.patricia.parches.application.port.out.ParcheEventPublisherOut;
import ingprompt.patricia.parches.application.port.out.ParcheRepositoryOutPort;
import ingprompt.patricia.parches.domain.exception.InvalidInviteTokenException;
import ingprompt.patricia.parches.domain.exception.NotParcheOwnerException;
import ingprompt.patricia.parches.domain.exception.ParcheFullException;
import ingprompt.patricia.parches.domain.exception.ParcheIsPublicException;
import ingprompt.patricia.parches.domain.exception.ParcheNotFoundException;
import ingprompt.patricia.parches.domain.model.Parche;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class InviteService implements InviteUserCase {
    private final InviteRepositoryOutPort inviteRepositoryOutPort;
    private final InviteTokenGeneratorPort tokenGeneratorPort;
    private final ParcheRepositoryOutPort parcheRepositoryOutPort;
    private final ParcheEventPublisherOut eventPublisher;


    @Override
    public String inviteUser(UUID parcheId, UUID requesterId) {
        Parche parche = parcheRepositoryOutPort.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));
        if (!parche.isOwnedBy(requesterId)) {
            throw new NotParcheOwnerException(parcheId, requesterId);
        }
        if (parche.isPublic()) {
            throw new ParcheIsPublicException(parcheId);
        }
        if (parche.isFull()) {
            throw new ParcheFullException(parcheId);
        }

        String token = tokenGeneratorPort.generateToken();
        inviteRepositoryOutPort.saveInvite(token, parcheId, INVITE_TTL_SECONDS);
        return token;
    }

    @Override
    @Transactional
    public void acceptInvite(String inviteToken, UUID acceptingUserId) {
        UUID parcheId = inviteRepositoryOutPort.findParcheIdByToken(inviteToken).orElseThrow(InvalidInviteTokenException::new);
        Parche parche = parcheRepositoryOutPort.findById(parcheId).orElseThrow(() -> new ParcheNotFoundException(parcheId));

        // Idempotency: if user is already a member, do nothing.
        // The token is NOT deleted here — it stays valid (multi-use) until TTL.
        if (parche.hasMember(acceptingUserId)) {
            return;
        }

        if (parche.isFull()) {
            throw new ParcheFullException(parcheId);
        }

        parche.addMember(acceptingUserId);
        parcheRepositoryOutPort.save(parche);
        eventPublisher.publishNewParcheMember(parcheId, acceptingUserId);
    }
}
