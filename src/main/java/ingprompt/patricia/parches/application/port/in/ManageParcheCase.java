package ingprompt.patricia.parches.application.port.in;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.model.Parche;

import java.util.UUID;

public interface ManageParcheCase {
    Parche createParche(String name, ParcheCategory category, int num, UUID ownerId, String description, Visibility visibility);
    void deleteParche (UUID parcheId, UUID ownerId);
}
