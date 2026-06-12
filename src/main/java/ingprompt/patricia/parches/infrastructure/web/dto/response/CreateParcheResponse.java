package ingprompt.patricia.parches.infrastructure.web.dto.response;

import ingprompt.patricia.parches.domain.enums.ParcheStatus;
import ingprompt.patricia.parches.domain.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateParcheResponse {
    private UUID parcheId;
    private String name;
    private String description;
    private Visibility visibility;
    private ParcheStatus status;
}
