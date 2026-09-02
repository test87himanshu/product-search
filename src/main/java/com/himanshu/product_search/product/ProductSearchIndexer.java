package com.himanshu.product_search.product;

import com.himanshu.product_search.product.mapper.ProductMapper;
import com.himanshu.product_search.product.search.ProductSearchRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductSearchIndexer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ProductMapper productMapper;

    public ProductSearchIndexer(
            ProductRepository productRepository,
            ProductSearchRepository productSearchRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productSearchRepository = productSearchRepository;
        this.productMapper = productMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        productSearchRepository.saveAll(
                productRepository.findAll().stream()
                        .map(productMapper::toSearchDocument)
                        .toList()
        );
    }
}
