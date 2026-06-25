package ingprompt.patricia.parches.infrastructure.messaging.listener;

import ingprompt.patricia.parches.application.port.in.ParcheProvisioningCase;
import ingprompt.patricia.parches.infrastructure.messaging.event.CollaborationReadyEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.CommunicationReadyEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ParcheProvisioningListenerTest {

    @Mock
    private ParcheProvisioningCase provisioningCase;
    @InjectMocks
    private ParcheProvisioningListener listener;

    @Test
    void onCommunicationReady_assignsChannels() {
        UUID parcheId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        UUID voiceId = UUID.randomUUID();

        listener.onCommunicationReady(new CommunicationReadyEvent(parcheId, chatId, voiceId));

        verify(provisioningCase).assignCommunicationChannels(parcheId, chatId, voiceId);
    }

    @Test
    void onCollaborationReady_assignsTools() {
        UUID parcheId = UUID.randomUUID();
        UUID parquesId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();

        listener.onCollaborationReady(new CollaborationReadyEvent(parcheId, parquesId, canvasId));

        verify(provisioningCase).assignCollaborationTools(parcheId, parquesId, canvasId);
    }
}
