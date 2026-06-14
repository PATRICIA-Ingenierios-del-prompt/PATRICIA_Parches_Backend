package ingprompt.patricia.parches.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Inbound: published by the Event MS when an event is deleted.
 * {@code parcheId} is non-null only if the deleted event was linked to a parche;
 * Event MS should skip publishing if the event was standalone.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDeletedEvent {
    private UUID eventId;
    private UUID parcheId;
}
