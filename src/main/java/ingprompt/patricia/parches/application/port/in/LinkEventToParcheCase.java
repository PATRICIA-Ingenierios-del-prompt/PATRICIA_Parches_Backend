package ingprompt.patricia.parches.application.port.in;

import java.util.UUID;

public interface LinkEventToParcheCase {
    void linkEventToParche(UUID parcheId, UUID eventId, UUID userId);
    void unlinkEventFromParche(UUID parcheId, UUID eventId);
}
