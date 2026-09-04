package com.himanshu.product_search.kafka;

import tools.jackson.databind.ObjectMapper;
import com.himanshu.product_search.product.search.ProductSearchDocument;
import com.himanshu.product_search.product.search.ProductSearchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    private final ObjectMapper objectMapper;
    private final ProductSearchService productSearchService;

    public ProductEventConsumer(
            ObjectMapper objectMapper,
            ProductSearchService productSearchService
    ) {
        this.objectMapper = objectMapper;
        this.productSearchService = productSearchService;
    }

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_EVENTS,
            groupId = "product-search-indexer"
    )
    public void consume(String message) {

        try {
            ProductKafkaEvent event =
                    objectMapper.readValue(
                            message,
                            ProductKafkaEvent.class
                    );

            switch (event.getEventType()) {

                case "PRODUCT_CREATED":
                case "PRODUCT_UPDATED":

                    ProductSearchDocument product =
                            objectMapper.readValue(
                                    event.getPayload(),
                                    ProductSearchDocument.class
                            );

                    productSearchService.index(product);
                    break;

                case "PRODUCT_DELETED":

                    productSearchService.delete(
                            Long.valueOf(event.getAggregateId())
                    );
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unknown product event type: "
                                    + event.getEventType()
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
