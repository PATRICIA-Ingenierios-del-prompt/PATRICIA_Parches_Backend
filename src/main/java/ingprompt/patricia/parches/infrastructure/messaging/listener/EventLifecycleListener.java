package ingprompt.patricia.parches.infrastructure.messaging.listener;

import ingprompt.patricia.parches.application.port.in.LinkEventToParcheCase;
import ingprompt.patricia.parches.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.parches.infrastructure.messaging.event.EventDeletedEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.EventLinkedToParcheEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventLifecycleListener {
    private final LinkEventToParcheCase linkEventToParcheCase;

    @RabbitListener(queues = RabbitMQConfig.EVENT_LINKED_QUEUE)
    public void onEventLinkedToParche(EventLinkedToParcheEvent event) {
        log.info("Linking event {} to parche {} (by user {})",
                event.getEventId(), event.getParcheId(), event.getUserId());
        linkEventToParcheCase.linkEventToParche(event.getParcheId(), event.getEventId(), event.getUserId());
    }

    @RabbitListener(queues = RabbitMQConfig.EVENT_DELETED_QUEUE)
    public void onEventDeleted(EventDeletedEvent event) {
        if (event.getParcheId() == null) {
            return;
        }
        log.info("Unlinking deleted event {} from parche {}", event.getEventId(), event.getParcheId());
        linkEventToParcheCase.unlinkEventFromParche(event.getParcheId(), event.getEventId());
    }
}
