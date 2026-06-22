package ingprompt.patricia.parches.domain.exception;

import java.util.UUID;

/**
 * Thrown when concurrent writers raced on the same parche and retries were exhausted.
 * Maps to HTTP 409. The client may safely retry — the operation is idempotent.
 */
public class ParcheConcurrentModificationException extends RuntimeException {
    public ParcheConcurrentModificationException(UUID parcheId) {
        super("Parche " + parcheId + " is being modified concurrently; please retry");
    }
}
