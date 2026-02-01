package com.minzetsu.ecommerce.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "ecommerce.events";
    public static final String DLX = "ecommerce.events.dlx";
    public static final String QUEUE_SEARCH = "ecommerce.search.queue";
    public static final String QUEUE_NOTIFICATION = "ecommerce.notification.queue";
    public static final String QUEUE_ANALYTICS = "ecommerce.analytics.queue";
    public static final String QUEUE_CHATBOT_CACHE = "ecommerce.chatbot.cache.queue";
    public static final String ROUTING_ALL = "event.#";
    public static final String ROUTING_SEARCH = "event.search";
    public static final String ROUTING_NOTIFICATION = "event.notification";
    public static final String ROUTING_ANALYTICS = "event.analytics";
    public static final String ROUTING_CHATBOT = "event.chatbot";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue searchQueue() {
        return QueueBuilder.durable(QUEUE_SEARCH)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_SEARCH + ".dlq")
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_NOTIFICATION + ".dlq")
                .build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(QUEUE_ANALYTICS)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_ANALYTICS + ".dlq")
                .build();
    }

    @Bean
    public Queue chatbotCacheQueue() {
        return QueueBuilder.durable(QUEUE_CHATBOT_CACHE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_CHATBOT_CACHE + ".dlq")
                .build();
    }

    @Bean
    public Queue searchDlq() {
        return QueueBuilder.durable(QUEUE_SEARCH + ".dlq").build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION + ".dlq").build();
    }

    @Bean
    public Queue analyticsDlq() {
        return QueueBuilder.durable(QUEUE_ANALYTICS + ".dlq").build();
    }

    @Bean
    public Queue chatbotCacheDlq() {
        return QueueBuilder.durable(QUEUE_CHATBOT_CACHE + ".dlq").build();
    }

    @Bean
    public Binding bindSearchQueue(TopicExchange eventsExchange, Queue searchQueue) {
        return BindingBuilder.bind(searchQueue).to(eventsExchange).with(ROUTING_SEARCH);
    }

    @Bean
    public Binding bindNotificationQueue(TopicExchange eventsExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with(ROUTING_NOTIFICATION);
    }

    @Bean
    public Binding bindAnalyticsQueue(TopicExchange eventsExchange, Queue analyticsQueue) {
        return BindingBuilder.bind(analyticsQueue).to(eventsExchange).with(ROUTING_ANALYTICS);
    }

    @Bean
    public Binding bindChatbotQueue(TopicExchange eventsExchange, Queue chatbotCacheQueue) {
        return BindingBuilder.bind(chatbotCacheQueue).to(eventsExchange).with(ROUTING_CHATBOT);
    }

    @Bean
    public Binding bindChatbotQueueToSearch(TopicExchange eventsExchange, Queue chatbotCacheQueue) {
        return BindingBuilder.bind(chatbotCacheQueue).to(eventsExchange).with(ROUTING_SEARCH);
    }

    @Bean
    public Binding bindSearchDlq(DirectExchange dlxExchange, Queue searchDlq) {
        return BindingBuilder.bind(searchDlq).to(dlxExchange).with(QUEUE_SEARCH + ".dlq");
    }

    @Bean
    public Binding bindNotificationDlq(DirectExchange dlxExchange, Queue notificationDlq) {
        return BindingBuilder.bind(notificationDlq).to(dlxExchange).with(QUEUE_NOTIFICATION + ".dlq");
    }

    @Bean
    public Binding bindAnalyticsDlq(DirectExchange dlxExchange, Queue analyticsDlq) {
        return BindingBuilder.bind(analyticsDlq).to(dlxExchange).with(QUEUE_ANALYTICS + ".dlq");
    }

    @Bean
    public Binding bindChatbotCacheDlq(DirectExchange dlxExchange, Queue chatbotCacheDlq) {
        return BindingBuilder.bind(chatbotCacheDlq).to(dlxExchange).with(QUEUE_CHATBOT_CACHE + ".dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        return factory;
    }
}
