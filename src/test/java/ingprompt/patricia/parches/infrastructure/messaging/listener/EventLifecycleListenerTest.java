package ingprompt.patricia.parches.infrastructure.messaging.listener;

import ingprompt.patricia.parches.application.port.in.LinkEventToParcheCase;
import ingprompt.patricia.parches.infrastructure.messaging.event.EventDeletedEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.EventLinkedToParcheEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EventLifecycleListenerTest {

    @Mock
    private LinkEventToParcheCase linkEventToParcheCase;
    @InjectMocks
    private EventLifecycleListener listener;

    @Test
    void onEventLinkedToParche_linksEvent() {
        UUID eventId = UUID.randomUUID();
        UUID parcheId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        listener.onEventLinkedToParche(new EventLinkedToParcheEvent(eventId, parcheId, userId));

        verify(linkEventToParcheCase).linkEventToParche(parcheId, eventId, userId);
    }

    @Test
    void onEventDeleted_withParche_unlinks() {
        UUID eventId = UUID.randomUUID();
        UUID parcheId = UUID.randomUUID();

        listener.onEventDeleted(new EventDeletedEvent(eventId, parcheId));

        verify(linkEventToParcheCase).unlinkEventFromParche(parcheId, eventId);
    }

    @Test
    void onEventDeleted_withoutParche_isNoOp() {
        listener.onEventDeleted(new EventDeletedEvent(UUID.randomUUID(), null));

        verifyNoInteractions(linkEventToParcheCase);
    }
}
