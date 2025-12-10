package com.minzetsu.ecommerce.product.repository;

import com.minzetsu.ecommerce.product.dto.filter.CategoryFilter;
import com.minzetsu.ecommerce.product.entity.Category;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class CategorySpecification {

    public static Specification<Category> filter(CategoryFilter filter) {
        String name = filter.getName();
        String slug = filter.getSlug();
        Long parentId = filter.getParentId();

        LocalDateTime createdFrom = filter.getCreatedFrom();
        LocalDateTime createdTo = filter.getCreatedTo();
        LocalDateTime updatedFrom = filter.getUpdatedFrom();
        LocalDateTime updatedTo = filter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            var predicates = cb.conjunction();

            if (name != null && !name.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (slug != null && !slug.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("slug")), "%" + slug.toLowerCase() + "%"));
            }
            if (parentId != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("parent").get("id"), parentId));
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
