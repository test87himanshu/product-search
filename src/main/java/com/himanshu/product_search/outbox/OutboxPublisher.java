package com.himanshu.product_search.outbox;

import com.himanshu.product_search.kafka.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
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
            kafkaTemplate
                    .send(
                            KafkaTopics.PRODUCT_EVENTS,
                            event.getAggregateId(),
                            event.getPayload()
                    )
                    .get();

            event.setStatus(OutboxEventStatus.PUBLISHED);

            outboxEventRepository.save(event);

        } catch (Exception e) {
            System.err.println(
                    "Failed to publish outbox event: " + event.getId()
            );
        }
    }
}
