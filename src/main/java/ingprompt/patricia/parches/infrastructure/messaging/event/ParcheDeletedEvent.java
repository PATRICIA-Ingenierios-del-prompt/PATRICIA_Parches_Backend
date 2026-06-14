package ingprompt.patricia.parches.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Outbound: published when a Parche is deleted. {@code eventIds} carries the
 * snapshot of linked events at the moment of deletion so consumers
 * (notably the Event MS) can cascade-delete without re-querying.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcheDeletedEvent {
    private UUID parcheId;
    private UUID ownerId;
    private Set<UUID> eventIds;
}
