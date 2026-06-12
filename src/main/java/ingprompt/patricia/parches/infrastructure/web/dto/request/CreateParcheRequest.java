package ingprompt.patricia.parches.infrastructure.web.dto.request;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.Visibility;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateParcheRequest {
    private String name;
    private String description;
    private ParcheCategory category;
    private int maxCapacity;
    private Visibility visibility;
}
