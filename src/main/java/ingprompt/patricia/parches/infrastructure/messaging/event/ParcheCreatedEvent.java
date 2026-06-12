package ingprompt.patricia.parches.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcheCreatedEvent {
    private UUID parcheId;
    private UUID ownerId;
}
