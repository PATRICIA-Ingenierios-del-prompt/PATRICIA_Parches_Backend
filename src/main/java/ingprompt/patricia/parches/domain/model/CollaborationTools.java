package ingprompt.patricia.parches.domain.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.UUID;

@Data
@Embeddable
public class CollaborationTools {
    private UUID parquesId;
    private UUID canvasId;

    public boolean isReady() {
        return canvasId != null && parquesId != null;
    }
}
