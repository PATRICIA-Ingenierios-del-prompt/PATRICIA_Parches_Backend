package ingprompt.patricia.parches.domain.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.UUID;

@Data
@Embeddable
public class CommunicationChannels {
    private UUID chatId;
    private UUID voiceId;

    public boolean isReady() {
        return chatId != null && voiceId != null;
    }
}
