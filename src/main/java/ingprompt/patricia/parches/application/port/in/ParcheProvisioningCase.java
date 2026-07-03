package ingprompt.patricia.parches.application.port.in;

import java.util.UUID;

public interface ParcheProvisioningCase {
    void assignCommunicationChannels(UUID parcheId, UUID chatId, UUID voiceId);

    // Collaboration split into two services: each id arrives independently.
    void assignParquesTool(UUID parcheId, UUID parquesId);
    void assignBoardTool(UUID parcheId, UUID canvasId);
}
