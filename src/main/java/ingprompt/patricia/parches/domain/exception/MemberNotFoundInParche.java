package ingprompt.patricia.parches.domain.exception;

import java.util.UUID;

public class MemberNotFoundInParche extends RuntimeException {
    public MemberNotFoundInParche(UUID parcheId, UUID memberId) {
        super("Member identified with: " + memberId + " was not found in parche: " + parcheId);
    }
}
