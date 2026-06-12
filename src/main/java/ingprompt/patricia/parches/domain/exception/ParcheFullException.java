package ingprompt.patricia.parches.domain.exception;

import java.util.UUID;

public class ParcheFullException extends RuntimeException {
    public ParcheFullException(UUID parcheId) {
        super("Parche " + parcheId + " has reached its maximum capacity");
    }
}
