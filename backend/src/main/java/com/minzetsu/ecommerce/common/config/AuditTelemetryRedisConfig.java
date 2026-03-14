package com.minzetsu.ecommerce.common.config;

import com.minzetsu.ecommerce.common.audit.config.AuditTelemetryProperties;
import com.minzetsu.ecommerce.common.audit.telemetry.AuditTelemetrySubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class AuditTelemetryRedisConfig {
    private final AuditTelemetryProperties properties;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            AuditTelemetrySubscriber subscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        if (properties.isEnabled()) {
            container.addMessageListener(subscriber, new PatternTopic(properties.getChannel()));
        }
        return container;
    }
}




