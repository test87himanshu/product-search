package com.himanshu.product_search.product.search;

import com.himanshu.product_search.outbox.ProductEvent;
import com.himanshu.product_search.product.Product;
import com.himanshu.product_search.product.mapper.ProductMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;

@Service
public class ProductIndexService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductMapper productMapper;

    public ProductIndexService(
            ProductSearchRepository productSearchRepository,
            ElasticsearchOperations elasticsearchOperations,
            ProductMapper productMapper) {

        this.productSearchRepository = productSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.productMapper = productMapper;
    }

    public void index(Product product) {

        ProductSearchDocument document =
                new ProductSearchDocument();

        document.setId(product.getId());
        document.setName(product.getName());
        document.setDescription(product.getDescription());
        document.setBrand(product.getBrand());
        document.setCategory(product.getCategory());
        document.setPrice(product.getPrice());
        document.setRating(product.getRating());
        document.setInStock(product.getInStock());

        document.setSuggest(
                new Completion(new String[]{product.getName()})
        );

        index(document);
    }

    public void index(ProductEvent productEvent) {

        index(productMapper.toSearchDocument(productEvent));
    }

    public void index(ProductSearchDocument document) {

        productSearchRepository.save(document);
    }

    public void delete(Long productId) {

        elasticsearchOperations.delete(
                String.valueOf(productId),
                ProductSearchDocument.class
        );
    }
}
