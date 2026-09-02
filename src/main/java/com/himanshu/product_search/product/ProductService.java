package com.himanshu.product_search.product;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.himanshu.product_search.common.exception.ProductNotFoundException;
import com.himanshu.product_search.outbox.OutboxEvent;
import com.himanshu.product_search.outbox.OutboxEventRepository;
import com.himanshu.product_search.outbox.ProductEvent;
import com.himanshu.product_search.product.dto.CreateProductRequest;
import com.himanshu.product_search.product.dto.ProductResponse;
import com.himanshu.product_search.product.mapper.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ProductService(
            ProductRepository productRepository,
            ProductMapper productMapper,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setRating(request.getRating());
        product.setInStock(request.getInStock());

        Product savedProduct = productRepository.save(product);

        ProductEvent event = new ProductEvent(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getBrand(),
                savedProduct.getCategory(),
                savedProduct.getPrice(),
                savedProduct.getRating(),
                savedProduct.getInStock()
        );

        saveOutboxEvent(
                savedProduct.getId(),
                "PRODUCT_CREATED",
                event
        );

        return productMapper.toResponse(savedProduct);
    }

    public ProductResponse getProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return productMapper.toResponse(product);
    }

    public Page<ProductResponse> getProducts(Pageable pageable) {

        return productRepository.findAll(pageable)
                .map(productMapper::toResponse);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setRating(request.getRating());
        product.setInStock(request.getInStock());

        Product savedProduct = productRepository.save(product);

        ProductEvent event = new ProductEvent(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getBrand(),
                savedProduct.getCategory(),
                savedProduct.getPrice(),
                savedProduct.getRating(),
                savedProduct.getInStock()
        );

        saveOutboxEvent(
                savedProduct.getId(),
                "PRODUCT_UPDATED",
                event
        );

        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.delete(product);

        ProductEvent event = new ProductEvent(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getCategory(),
                product.getPrice(),
                product.getRating(),
                product.getInStock()
        );

        saveOutboxEvent(
                product.getId(),
                "PRODUCT_DELETED",
                event
        );
    }

    private void saveOutboxEvent(
            Long productId,
            String eventType,
            ProductEvent event) {

        try {
            OutboxEvent outboxEvent = new OutboxEvent();

            outboxEvent.setAggregateType("PRODUCT");
            outboxEvent.setAggregateId(productId.toString());
            outboxEvent.setEventType(eventType);
            outboxEvent.setPayload(objectMapper.writeValueAsString(event));
            outboxEvent.setCreatedAt(LocalDateTime.now());

            outboxEventRepository.save(outboxEvent);

        } catch (JacksonException e) {
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
}
