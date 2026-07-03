package ingprompt.patricia.parches.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {
    // ---------- Outbound exchange (owned by this service) ----------
    public static final String PARCHE_EXCHANGE = "parche.events";

    public static final String PARCHE_CREATED_ROUTING_KEY = "parche.created";
    public static final String PARCHE_DELETED_ROUTING_KEY = "parche.deleted";

    // ---------- Inbound: provisioning replies (owned by other services) ----------
    public static final String COMMUNICATION_READY_ROUTING_KEY = "parche.communication.ready";
    // Collaboration was split into two services: Parques (game) and Board (canvas).
    public static final String PARQUES_READY_ROUTING_KEY = "parche.parques.ready";
    public static final String BOARD_READY_ROUTING_KEY = "parche.board.ready";

    public static final String COMMUNICATION_READY_QUEUE = "parches.communication.ready.queue";
    public static final String PARQUES_READY_QUEUE = "parches.parques.ready.queue";
    public static final String BOARD_READY_QUEUE = "parches.board.ready.queue";

    // ---------- Inbound: event lifecycle (owned by Event MS) ----------
    public static final String EVENT_EXCHANGE = "event.events";

    public static final String EVENT_LINKED_ROUTING_KEY = "event.linked.to.parche";
    public static final String EVENT_DELETED_ROUTING_KEY = "event.deleted";

    public static final String EVENT_LINKED_QUEUE = "parches.event.linked.queue";
    public static final String EVENT_DELETED_QUEUE = "parches.event.deleted.queue";

    // ---------- Dead-letter infrastructure ----------
    public static final String DLX_EXCHANGE = "parches.dlx";
    private static final String DLQ_SUFFIX = ".dlq";

    @Bean
    public TopicExchange parcheExchange() {
        return new TopicExchange(PARCHE_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EVENT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange parchesDeadLetterExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    // ---- Inbound queues: every one now routes to the DLX on basicNack(requeue=false) ----

    @Bean
    public Queue communicationReadyQueue() {
        return dlqEnabled(COMMUNICATION_READY_QUEUE);
    }

    @Bean
    public Queue parquesReadyQueue() {
        return dlqEnabled(PARQUES_READY_QUEUE);
    }

    @Bean
    public Queue boardReadyQueue() {
        return dlqEnabled(BOARD_READY_QUEUE);
    }

    @Bean
    public Queue eventLinkedQueue() {
        return dlqEnabled(EVENT_LINKED_QUEUE);
    }

    @Bean
    public Queue eventDeletedQueue() {
        return dlqEnabled(EVENT_DELETED_QUEUE);
    }

    // ---- Per-queue DLQs (parked messages live here for inspection) ----

    @Bean
    public Queue communicationReadyDlq() {
        return QueueBuilder.durable(COMMUNICATION_READY_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue parquesReadyDlq() {
        return QueueBuilder.durable(PARQUES_READY_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue boardReadyDlq() {
        return QueueBuilder.durable(BOARD_READY_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue eventLinkedDlq() {
        return QueueBuilder.durable(EVENT_LINKED_QUEUE + DLQ_SUFFIX).build();
    }

    @Bean
    public Queue eventDeletedDlq() {
        return QueueBuilder.durable(EVENT_DELETED_QUEUE + DLQ_SUFFIX).build();
    }

    // ---- Bindings: live queues to their owning exchange + DLQs to the DLX ----

    @Bean
    public Binding communicationReadyBinding() {
        return BindingBuilder.bind(communicationReadyQueue()).to(parcheExchange()).with(COMMUNICATION_READY_ROUTING_KEY);
    }

    @Bean
    public Binding parquesReadyBinding() {
        return BindingBuilder.bind(parquesReadyQueue()).to(parcheExchange()).with(PARQUES_READY_ROUTING_KEY);
    }

    @Bean
    public Binding boardReadyBinding() {
        return BindingBuilder.bind(boardReadyQueue()).to(parcheExchange()).with(BOARD_READY_ROUTING_KEY);
    }

    @Bean
    public Binding eventLinkedBinding() {
        return BindingBuilder.bind(eventLinkedQueue()).to(eventExchange()).with(EVENT_LINKED_ROUTING_KEY);
    }

    @Bean
    public Binding eventDeletedBinding() {
        return BindingBuilder.bind(eventDeletedQueue()).to(eventExchange()).with(EVENT_DELETED_ROUTING_KEY);
    }

    @Bean
    public Binding communicationReadyDlqBinding() {
        return dlqBinding(COMMUNICATION_READY_QUEUE, communicationReadyDlq());
    }

    @Bean
    public Binding parquesReadyDlqBinding() {
        return dlqBinding(PARQUES_READY_QUEUE, parquesReadyDlq());
    }

    @Bean
    public Binding boardReadyDlqBinding() {
        return dlqBinding(BOARD_READY_QUEUE, boardReadyDlq());
    }

    @Bean
    public Binding eventLinkedDlqBinding() {
        return dlqBinding(EVENT_LINKED_QUEUE, eventLinkedDlq());
    }

    @Bean
    public Binding eventDeletedDlqBinding() {
        return dlqBinding(EVENT_DELETED_QUEUE, eventDeletedDlq());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // ---- RETRY ----
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(500L, 2.0, 10_000L)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RetryOperationsInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    // ---- helpers ----

    private Queue dlqEnabled(String queueName) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", queueName + DLQ_SUFFIX)
                .build();
    }

    private Binding dlqBinding(String queueName, Queue dlq) {
        return BindingBuilder.bind(dlq).to(parchesDeadLetterExchange()).with(queueName + DLQ_SUFFIX);
    }
}
