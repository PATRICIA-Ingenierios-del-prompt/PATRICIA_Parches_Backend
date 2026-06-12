package ingprompt.patricia.parches.domain.exception;

import java.util.UUID;

public class CannotRemoveOwnerException extends RuntimeException {
    public CannotRemoveOwnerException(UUID parcheId) {
        super("The owner cannot be removed from parche: " + parcheId);
    }
}
