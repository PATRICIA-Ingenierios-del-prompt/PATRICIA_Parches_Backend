package ingprompt.patricia.parches.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface ParcheEventPublisherOut {
    void publishParcheWasCreated(UUID parcheId, UUID ownerId);
    void publishNewParcheMember(UUID parcheId, UUID memberId);
    void publishParcheMemberExpelled(UUID parcheId, UUID memberId);
    void publishParcheDeleted(UUID parcheId, UUID ownerId, Set<UUID> eventIds);
}
