package com.himanshu.product_search.product;

import com.himanshu.product_search.common.exception.ProductNotFoundException;
import com.himanshu.product_search.product.dto.CreateProductRequest;
import com.himanshu.product_search.product.dto.ProductResponse;
import com.himanshu.product_search.product.mapper.ProductMapper;
import com.himanshu.product_search.product.search.ProductIndexService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductIndexService productIndexService;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            ProductIndexService productIndexService,
            ProductMapper productMapper) {

        this.productRepository = productRepository;
        this.productIndexService = productIndexService;
        this.productMapper = productMapper;
    }

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
        productIndexService.index(savedProduct);

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
        productIndexService.index(savedProduct);

        return productMapper.toResponse(savedProduct);
    }

    public void deleteProduct(Long id) {

        productRepository.deleteById(id);

        productIndexService.delete(id);
    }
}
