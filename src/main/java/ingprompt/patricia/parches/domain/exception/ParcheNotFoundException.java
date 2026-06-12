package ingprompt.patricia.parches.domain.exception;

import java.util.UUID;

public class ParcheNotFoundException extends RuntimeException {
    public ParcheNotFoundException(UUID parcheId) {
        super("Parche not found: " + parcheId);
    }
}
