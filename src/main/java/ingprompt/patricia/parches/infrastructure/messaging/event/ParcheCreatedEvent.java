package ingprompt.patricia.parches.infrastructure.messaging.event;

import ingprompt.patricia.parches.domain.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcheCreatedEvent {
    private UUID sourceEventId;
    private UUID parcheId;
    private String name;
    private Visibility visibility;
    private UUID ownerId;
}
