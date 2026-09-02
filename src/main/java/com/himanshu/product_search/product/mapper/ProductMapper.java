package com.himanshu.product_search.product.mapper;

import com.himanshu.product_search.product.Product;
import com.himanshu.product_search.product.dto.ProductResponse;
import com.himanshu.product_search.product.search.ProductSearchDocument;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getCategory(),
                product.getPrice(),
                product.getRating(),
                product.getInStock()
        );
    }

    public ProductSearchDocument toSearchDocument(Product product) {
        ProductSearchDocument document = new ProductSearchDocument();
        document.setId(product.getId());
        document.setName(product.getName());
        document.setDescription(product.getDescription());
        document.setBrand(product.getBrand());
        document.setCategory(product.getCategory());
        document.setPrice(product.getPrice());
        document.setRating(product.getRating());
        document.setInStock(product.getInStock());
        if (product.getName() != null) {
            document.setSuggest(new Completion(new String[]{product.getName()}));
        }
        return document;
    }
}
