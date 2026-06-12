package ingprompt.patricia.parches.infrastructure.messaging.listener;

import ingprompt.patricia.parches.application.port.in.ParcheProvisioningCase;
import ingprompt.patricia.parches.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.parches.infrastructure.messaging.event.CollaborationReadyEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.CommunicationReadyEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ParcheProvisioningListener {
    private final ParcheProvisioningCase provisioningCase;

    @RabbitListener(queues = RabbitMQConfig.COMMUNICATION_READY_QUEUE)
    public void onCommunicationReady(CommunicationReadyEvent event) {
        log.info("Communication ready for parche {}", event.getParcheId());
        provisioningCase.assignCommunicationChannels(event.getParcheId(), event.getChatId(), event.getVoiceId());
    }

    @RabbitListener(queues = RabbitMQConfig.COLLABORATION_READY_QUEUE)
    public void onCollaborationReady(CollaborationReadyEvent event) {
        log.info("Collaboration ready for parche {}", event.getParcheId());
        provisioningCase.assignCollaborationTools(event.getParcheId(), event.getParquesId(), event.getCanvasId());
    }
}
