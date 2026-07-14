package ingprompt.patricia.parches.infrastructure.web.dto.response;

import ingprompt.patricia.parches.domain.enums.ParcheCategory;
import ingprompt.patricia.parches.domain.enums.ParcheStatus;
import ingprompt.patricia.parches.domain.enums.Visibility;
import ingprompt.patricia.parches.domain.model.CommunicationChannels;
import ingprompt.patricia.parches.domain.model.Parche;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParcheResponse {
    private String name;
    private String description;
    private ParcheCategory category;
    private Visibility visibility;
    private ParcheStatus status;
    private int maxCapacity;
    private int memberCount;
    private String pictureUrl;
    // Chat/voice channel ids provisioned by the Communication MS (via
    // parche.created -> communication.ready). Null until provisioning lands;
    // the frontend needs chatId to open the real STOMP chat channel.
    private CommunicationChannels communication;

    public static ParcheResponse from(Parche parche) {
        return new ParcheResponse(
                parche.getName(),
                parche.getDescription(),
                parche.getCategory(),
                parche.getVisibility(),
                parche.getStatus(),
                parche.getMaxCapacity(),
                parche.getMembers().size(),
                parche.getPictureUrl(),
                parche.getCommunication()
        );
    }
}
