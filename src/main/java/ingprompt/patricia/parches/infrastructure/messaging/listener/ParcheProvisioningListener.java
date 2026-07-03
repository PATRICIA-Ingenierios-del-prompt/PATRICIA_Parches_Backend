package ingprompt.patricia.parches.infrastructure.messaging.listener;

import ingprompt.patricia.parches.application.port.in.ParcheProvisioningCase;
import ingprompt.patricia.parches.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.parches.infrastructure.messaging.event.BoardReadyEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.CommunicationReadyEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParquesReadyEvent;
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

    @RabbitListener(queues = RabbitMQConfig.PARQUES_READY_QUEUE)
    public void onParquesReady(ParquesReadyEvent event) {
        log.info("Parques ready for parche {}", event.getParcheId());
        provisioningCase.assignParquesTool(event.getParcheId(), event.getParquesId());
    }

    @RabbitListener(queues = RabbitMQConfig.BOARD_READY_QUEUE)
    public void onBoardReady(BoardReadyEvent event) {
        log.info("Board (canvas) ready for parche {}", event.getParcheId());
        provisioningCase.assignBoardTool(event.getParcheId(), event.getCanvasId());
    }
}
