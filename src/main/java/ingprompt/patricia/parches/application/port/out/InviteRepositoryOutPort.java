package ingprompt.patricia.parches.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface InviteRepositoryOutPort {
    void saveInvite(String token, UUID parcheId, long ttlSeconds);
    Optional<UUID> findParcheIdByToken(String token);
    void deleteInvite(String token);
}
