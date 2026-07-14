package ingprompt.patricia.parches.infrastructure.messaging.event;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcheMemberJoinedEvent {
    private UUID parcheId;
    private UUID memberId;
    private ParcheCategory category;
}
