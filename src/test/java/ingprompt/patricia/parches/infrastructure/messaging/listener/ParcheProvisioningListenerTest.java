package ingprompt.patricia.parches.infrastructure.messaging.listener;

import ingprompt.patricia.parches.application.port.in.ParcheProvisioningCase;
import ingprompt.patricia.parches.infrastructure.messaging.event.BoardReadyEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.CommunicationReadyEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParquesReadyEvent;
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
    void onParquesReady_assignsParquesTool() {
        UUID parcheId = UUID.randomUUID();
        UUID parquesId = UUID.randomUUID();

        listener.onParquesReady(new ParquesReadyEvent(parcheId, parquesId));

        verify(provisioningCase).assignParquesTool(parcheId, parquesId);
    }

    @Test
    void onBoardReady_assignsBoardTool() {
        UUID parcheId = UUID.randomUUID();
        UUID canvasId = UUID.randomUUID();

        listener.onBoardReady(new BoardReadyEvent(parcheId, canvasId));

        verify(provisioningCase).assignBoardTool(parcheId, canvasId);
    }
}
