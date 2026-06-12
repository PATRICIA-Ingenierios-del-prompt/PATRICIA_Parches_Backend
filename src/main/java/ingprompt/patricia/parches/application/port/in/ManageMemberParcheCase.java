package ingprompt.patricia.parches.application.port.in;

import java.util.UUID;

public interface ManageMemberParcheCase {
    void joinPublicParche(UUID parcheId, UUID userId);
    void removeMemberFromParche(UUID parcheId, UUID memberId, UUID requesterId);
}
