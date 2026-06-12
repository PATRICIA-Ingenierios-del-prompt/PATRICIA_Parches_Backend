package ingprompt.patricia.parches.domain.exception;

import java.util.UUID;

public class ParcheIsPrivateException extends RuntimeException {
    public ParcheIsPrivateException(UUID parcheId) {
        super("Parche " + parcheId + " is private and requires an invite token");
    }
}
