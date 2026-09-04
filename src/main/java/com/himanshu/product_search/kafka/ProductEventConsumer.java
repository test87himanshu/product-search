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

            ProductEvent productEvent =
                    objectMapper.readValue(
                            kafkaEvent.getPayload(),
                            ProductEvent.class
                    );

            productIndexService.index(productEvent);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to process product event",
                    e
            );
        }
    }
}
