package ingprompt.patricia.parches.application.port.out;

import java.util.UUID;

public interface ParcheEventPublisherOut {
    void publishParcheWasCreated(UUID parcheId, UUID ownerId);
    void publishNewParcheMember(UUID parcheId, UUID memberId);
    void publishParcheMemberExpelled(UUID parcheId, UUID memberId);
}
