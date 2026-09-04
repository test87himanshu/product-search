package com.himanshu.product_search.product.search;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

@Service
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public ProductSearchService(
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public void index(ProductSearchDocument document) {
        elasticsearchOperations.save(document);
    }

    public void delete(Long productId) {
        elasticsearchOperations.delete(
                String.valueOf(productId),
                ProductSearchDocument.class
        );
    }
}
