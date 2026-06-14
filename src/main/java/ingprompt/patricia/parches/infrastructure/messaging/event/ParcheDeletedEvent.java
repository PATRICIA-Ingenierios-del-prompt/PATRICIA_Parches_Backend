package ingprompt.patricia.parches.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcheDeletedEvent {
    private UUID parcheId;
    private UUID ownerId;
    private Set<UUID> eventIds;
}
