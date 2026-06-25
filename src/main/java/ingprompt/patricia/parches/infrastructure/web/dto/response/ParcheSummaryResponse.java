package ingprompt.patricia.parches.infrastructure.web.dto.response;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.ParcheStatus;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.model.Parche;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParcheSummaryResponse {
    private UUID parcheId;
    private String name;
    private String description;
    private ParcheCategory category;
    private Visibility visibility;
    private ParcheStatus status;
    private int maxCapacity;
    private int memberCount;
    private String pictureUrl;

    public static ParcheSummaryResponse from(Parche parche) {
        return new ParcheSummaryResponse(
                parche.getParcheId(),
                parche.getName(),
                parche.getDescription(),
                parche.getCategory(),
                parche.getVisibility(),
                parche.getStatus(),
                parche.getMaxCapacity(),
                parche.getMembers().size(),
                parche.getPictureUrl()
        );
    }
}
