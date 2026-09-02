package com.himanshu.product_search.product.dto;

public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String brand;
    private String category;
    private Double price;
    private Double rating;
    private Boolean inStock;

    public ProductResponse(
            Long id,
            String name,
            String description,
            String brand,
            String category,
            Double price,
            Double rating,
            Boolean inStock) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.rating = rating;
        this.inStock = inStock;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public Double getPrice() {
        return price;
    }

    public Double getRating() {
        return rating;
    }

    public Boolean getInStock() {
        return inStock;
    }
}
