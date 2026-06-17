package ingprompt.patricia.parches.application.port.in;

import ingprompt.patricia.parches.domain.model.Parche;

import java.util.Set;
import java.util.UUID;


public interface ParcheQueryCase {
    Parche getParcheById(UUID parcheId);
    Set<UUID> getEventsOfParche(UUID parcheId);
}
