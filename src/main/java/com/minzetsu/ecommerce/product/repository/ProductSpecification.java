package com.minzetsu.ecommerce.product.repository;

import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductSpecification {

    public static Specification<Product> filter(ProductFilter filter) {
        Long categoryId = filter.getCategoryId();
        String name = filter.getName();
        String slug = filter.getSlug();
        String sku = filter.getSku();
        BigDecimal minPrice = filter.getMinPrice();
        BigDecimal maxPrice = filter.getMaxPrice();
        String status = filter.getStatus();

        LocalDateTime createdFrom = filter.getCreatedFrom();
        LocalDateTime createdTo = filter.getCreatedTo();
        LocalDateTime updatedFrom = filter.getUpdatedFrom();
        LocalDateTime updatedTo = filter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            var predicates = cb.conjunction();

            if (categoryId != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("category").get("id"), categoryId));
            }
            if (name != null && !name.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (slug != null && !slug.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("slug")), "%" + slug.toLowerCase() + "%"));
            }
            if (sku != null && !sku.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("sku")), "%" + sku.toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates = cb.and(predicates,
                        cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (status != null && !status.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }
            if (createdFrom != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates = cb.and(predicates,
                        cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }
            if (updatedFrom != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom));
            }
            if (updatedTo != null) {
                predicates = cb.and(predicates,
                        cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo));
            }

            return predicates;
        };
    }
}
