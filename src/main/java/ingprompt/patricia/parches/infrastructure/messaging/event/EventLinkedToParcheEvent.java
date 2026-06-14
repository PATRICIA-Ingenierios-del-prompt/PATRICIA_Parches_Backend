package ingprompt.patricia.parches.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Inbound: published by the Event MS when an event is created and linked to a parche.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLinkedToParcheEvent {
    private UUID eventId;
    private UUID parcheId;
    private UUID userId;
}
