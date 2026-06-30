package ingprompt.patricia.parches.infrastructure.messaging.publisher;

import ingprompt.patricia.parches.infrastructure.messaging.config.RabbitMQConfig;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParcheCreatedEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParcheDeletedEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParcheMemberExpelledEvent;
import ingprompt.patricia.parches.infrastructure.messaging.event.ParcheMemberJoinedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ParcheEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ParcheEventPublisherAdapter adapter;

    private final UUID parcheId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new ParcheEventPublisherAdapter(rabbitTemplate);
    }

    @Test
    void publishParcheWasCreated_sendsCreatedEvent() {
        adapter.publishParcheWasCreated(parcheId, userId, ingprompt.patricia.parches.domain.enums.Visibility.PUBLIC);

        ArgumentCaptor<ParcheCreatedEvent> body = ArgumentCaptor.forClass(ParcheCreatedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.PARCHE_EXCHANGE), eq(RabbitMQConfig.PARCHE_CREATED_ROUTING_KEY), body.capture());
        assertThat(body.getValue().getParcheId()).isEqualTo(parcheId);
        assertThat(body.getValue().getOwnerId()).isEqualTo(userId);
        assertThat(body.getValue().getVisibility()).isEqualTo(ingprompt.patricia.parches.domain.enums.Visibility.PUBLIC);
    }

    @Test
    void publishNewParcheMember_sendsJoinedEvent() {
        adapter.publishNewParcheMember(parcheId, userId);

        ArgumentCaptor<ParcheMemberJoinedEvent> body = ArgumentCaptor.forClass(ParcheMemberJoinedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.PARCHE_EXCHANGE), eq("parche.member.joined"), body.capture());
        assertThat(body.getValue().getMemberId()).isEqualTo(userId);
    }

    @Test
    void publishParcheMemberExpelled_sendsExpelledEvent() {
        adapter.publishParcheMemberExpelled(parcheId, userId);

        ArgumentCaptor<ParcheMemberExpelledEvent> body = ArgumentCaptor.forClass(ParcheMemberExpelledEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.PARCHE_EXCHANGE), eq("parche.member.expelled"), body.capture());
        assertThat(body.getValue().getMemberId()).isEqualTo(userId);
    }

    @Test
    void publishParcheDeleted_sendsDeletedEventWithEventIds() {
        Set<UUID> eventIds = Set.of(UUID.randomUUID());
        adapter.publishParcheDeleted(parcheId, userId, eventIds);

        ArgumentCaptor<ParcheDeletedEvent> body = ArgumentCaptor.forClass(ParcheDeletedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.PARCHE_EXCHANGE), eq(RabbitMQConfig.PARCHE_DELETED_ROUTING_KEY), body.capture());
        assertThat(body.getValue().getEventIds()).isEqualTo(eventIds);
    }
}
