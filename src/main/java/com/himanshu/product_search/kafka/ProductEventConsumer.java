package com.himanshu.product_search.kafka;

import tools.jackson.databind.ObjectMapper;
import com.himanshu.product_search.outbox.ProductEvent;
import com.himanshu.product_search.product.search.ProductIndexService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    private final ObjectMapper objectMapper;
    private final ProductIndexService productIndexService;

    public ProductEventConsumer(
            ObjectMapper objectMapper,
            ProductIndexService productIndexService
    ) {
        this.objectMapper = objectMapper;
        this.productIndexService = productIndexService;
    }

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_EVENTS,
            groupId = "product-search-indexer"
    )
    public void consume(String message) {

        try {
            ProductKafkaEvent kafkaEvent =
                    objectMapper.readValue(
                            message,
                            ProductKafkaEvent.class
                    );

            switch (kafkaEvent.getEventType()) {

                case "PRODUCT_CREATED":
                case "PRODUCT_UPDATED":
                    ProductEvent productEvent =
                            objectMapper.readValue(
                                    kafkaEvent.getPayload(),
                                    ProductEvent.class
                            );
                    productIndexService.index(productEvent);
                    break;

                case "PRODUCT_DELETED":
                    productIndexService.delete(
                            Long.valueOf(kafkaEvent.getAggregateId())
                    );
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unknown product event type: "
                                    + kafkaEvent.getEventType()
                    );
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to process product event",
                    e
            );
        }
    }
}
