package ingprompt.patricia.parches.infrastructure.messaging.publisher;

import ingprompt.patricia.parches.application.port.out.ParcheEventPublisherOut;
import ingprompt.patricia.parches.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParcheCreatedEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParcheDeletedEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParcheMemberExpelledEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParcheMemberJoinedEvent;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ParcheEventPublisherAdapter implements ParcheEventPublisherOut {
    private static final String MEMBER_JOINED_ROUTING_KEY = "parche.member.joined";
    private static final String MEMBER_EXPELLED_ROUTING_KEY = "parche.member.expelled";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishParcheWasCreated(UUID parcheId, UUID ownerId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.PARCHE_EXCHANGE, RabbitMQConfig.PARCHE_CREATED_ROUTING_KEY, new ParcheCreatedEvent(parcheId, ownerId));
    }

    @Override
    public void publishNewParcheMember(UUID parcheId, UUID memberId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.PARCHE_EXCHANGE, MEMBER_JOINED_ROUTING_KEY, new ParcheMemberJoinedEvent(parcheId, memberId));
    }

    @Override
    public void publishParcheMemberExpelled(UUID parcheId, UUID memberId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.PARCHE_EXCHANGE, MEMBER_EXPELLED_ROUTING_KEY, new ParcheMemberExpelledEvent(parcheId, memberId));
    }

    @Override
    public void publishParcheDeleted(UUID parcheId, UUID ownerId, Set<UUID> eventIds) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.PARCHE_EXCHANGE, RabbitMQConfig.PARCHE_DELETED_ROUTING_KEY, new ParcheDeletedEvent(parcheId, ownerId, eventIds));
    }
}
