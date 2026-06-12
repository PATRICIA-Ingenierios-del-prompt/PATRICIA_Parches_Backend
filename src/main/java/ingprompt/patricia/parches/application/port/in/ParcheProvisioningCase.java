package ingprompt.patricia.parches.application.port.in;

import java.util.UUID;

public interface ParcheProvisioningCase {
    void assignCommunicationChannels(UUID parcheId, UUID chatId, UUID voiceId);
    void assignCollaborationTools(UUID parcheId, UUID parquesId, UUID canvasId);
}
