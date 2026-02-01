package com.minzetsu.ecommerce.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "products", createIndex = false)
public class ProductDocument {
    @Id
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private String status;
    private Double price;

    public ProductDocument() {
    }

    public ProductDocument(Long id, String name, String description, String categoryName, String status, Double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.status = status;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
