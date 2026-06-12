package ingprompt.patricia.parches.domain.exception;

import java.util.UUID;

public class NotParcheOwnerException extends RuntimeException {
    public NotParcheOwnerException(UUID parcheId, UUID userId) {
        super("User " + userId + " is not the owner of parche " + parcheId);
    }
}
