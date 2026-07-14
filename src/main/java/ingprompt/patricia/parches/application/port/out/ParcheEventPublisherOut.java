package ingprompt.patricia.parches.application.port.out;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;

import java.util.Set;
import java.util.UUID;

public interface ParcheEventPublisherOut {
    void publishParcheWasCreated(UUID parcheId, String name, UUID ownerId, Visibility visibility);
    void publishNewParcheMember(UUID parcheId, UUID memberId, ParcheCategory category);
    void publishParcheMemberExpelled(UUID parcheId, UUID memberId);
    void publishParcheDeleted(UUID parcheId, UUID ownerId, Set<UUID> eventIds);
}
