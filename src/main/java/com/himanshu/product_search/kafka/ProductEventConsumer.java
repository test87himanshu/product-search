package com.himanshu.product_search.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_EVENTS,
            groupId = "product-search-indexer"
    )
    public void consume(String message) {

        System.out.println(
                "Received product event: " + message
        );
    }
}
