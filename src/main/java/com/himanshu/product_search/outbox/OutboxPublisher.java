package com.himanshu.product_search.outbox;

import tools.jackson.databind.ObjectMapper;
import com.himanshu.product_search.kafka.KafkaTopics;
import com.himanshu.product_search.kafka.ProductKafkaEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findTop100ByStatusOrderByIdAsc(
                        OutboxEventStatus.PENDING
                );

        for (OutboxEvent event : events) {
            publish(event);
        }
    }

    private void publish(OutboxEvent event) {

        try {
            ProductKafkaEvent kafkaEvent = new ProductKafkaEvent();

            kafkaEvent.setEventType(event.getEventType());
            kafkaEvent.setAggregateId(event.getAggregateId());
            kafkaEvent.setPayload(event.getPayload());

            String message =
                    objectMapper.writeValueAsString(kafkaEvent);

            kafkaTemplate
                    .send(
                            KafkaTopics.PRODUCT_EVENTS,
                            event.getAggregateId(),
                            message
                    )
                    .get();

            event.setStatus(OutboxEventStatus.PUBLISHED);

            outboxEventRepository.save(event);

        } catch (Exception e) {
            System.err.println(
                    "Failed to publish outbox event: "
                            + event.getId()
            );
        }
    }
}
