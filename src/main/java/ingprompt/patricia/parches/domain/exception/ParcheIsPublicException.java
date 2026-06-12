package ingprompt.patricia.parches.domain.exception;

import java.util.UUID;

public class ParcheIsPublicException extends RuntimeException {
    public ParcheIsPublicException(UUID parcheId) {
        super("Parche " + parcheId + " is public and does not require invites");
    }
}
