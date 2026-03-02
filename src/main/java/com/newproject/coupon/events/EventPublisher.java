package com.newproject.coupon.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String topic;

    public EventPublisher(
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper,
        @Value("${coupon.events.enabled:true}") boolean enabled,
        @Value("${coupon.events.topic:coupon.events}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.topic = topic;
    }

    public void publish(String eventType, String aggregateType, String aggregateId, Object payload) {
        if (!enabled) {
            return;
        }

        DomainEvent event = new DomainEvent(
            eventType,
            aggregateType,
            aggregateId,
            OffsetDateTime.now(),
            payload
        );

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, aggregateId, json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize domain event", e);
        }
    }
}
