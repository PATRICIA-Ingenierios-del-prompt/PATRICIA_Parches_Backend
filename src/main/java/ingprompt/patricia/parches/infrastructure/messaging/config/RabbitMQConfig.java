package ingprompt.patricia.parches.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ---------- Outbound exchange (owned by this service) ----------
    public static final String PARCHE_EXCHANGE = "parche.events";

    public static final String PARCHE_CREATED_ROUTING_KEY = "parche.created";
    public static final String PARCHE_DELETED_ROUTING_KEY = "parche.deleted";

    // ---------- Inbound: provisioning replies (owned by other services) ----------
    public static final String COMMUNICATION_READY_ROUTING_KEY = "parche.communication.ready";
    public static final String COLLABORATION_READY_ROUTING_KEY = "parche.collaboration.ready";

    public static final String COMMUNICATION_READY_QUEUE = "parches.communication.ready.queue";
    public static final String COLLABORATION_READY_QUEUE = "parches.collaboration.ready.queue";

    // ---------- Inbound: event lifecycle (owned by Event MS) ----------
    public static final String EVENT_EXCHANGE = "event.events";

    public static final String EVENT_LINKED_ROUTING_KEY = "event.linked.to.parche";
    public static final String EVENT_DELETED_ROUTING_KEY = "event.deleted";

    public static final String EVENT_LINKED_QUEUE = "parches.event.linked.queue";
    public static final String EVENT_DELETED_QUEUE = "parches.event.deleted.queue";

    @Bean
    public TopicExchange parcheExchange() {
        return new TopicExchange(PARCHE_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue communicationReadyQueue() {
        return new Queue(COMMUNICATION_READY_QUEUE, true);
    }

    @Bean
    public Queue collaborationReadyQueue() {
        return new Queue(COLLABORATION_READY_QUEUE, true);
    }

    @Bean
    public Queue eventLinkedQueue() {
        return new Queue(EVENT_LINKED_QUEUE, true);
    }

    @Bean
    public Queue eventDeletedQueue() {
        return new Queue(EVENT_DELETED_QUEUE, true);
    }

    @Bean
    public Binding communicationReadyBinding() {
        return BindingBuilder.bind(communicationReadyQueue())
                .to(parcheExchange())
                .with(COMMUNICATION_READY_ROUTING_KEY);
    }

    @Bean
    public Binding collaborationReadyBinding() {
        return BindingBuilder.bind(collaborationReadyQueue())
                .to(parcheExchange())
                .with(COLLABORATION_READY_ROUTING_KEY);
    }

    @Bean
    public Binding eventLinkedBinding() {
        return BindingBuilder.bind(eventLinkedQueue())
                .to(eventExchange())
                .with(EVENT_LINKED_ROUTING_KEY);
    }

    @Bean
    public Binding eventDeletedBinding() {
        return BindingBuilder.bind(eventDeletedQueue())
                .to(eventExchange())
                .with(EVENT_DELETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
