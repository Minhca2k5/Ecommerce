package com.minzetsu.ecommerce.review.repository;

import com.minzetsu.ecommerce.review.dto.filter.ReviewFilter;
import com.minzetsu.ecommerce.review.entity.Review;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ReviewSpecification {

    public static Specification<Review> filter(ReviewFilter filter) {
        Long productId = filter.getProductId();
        Long userId = filter.getUserId();
        Integer minRating = filter.getMinRating();
        Integer maxRating = filter.getMaxRating();
        String commentKeyword = filter.getCommentKeyword();
        var createdFrom = filter.getCreatedFrom();
        var createdTo = filter.getCreatedTo();
        var updatedFrom = filter.getUpdatedFrom();
        var updatedTo = filter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            Predicate predicates = cb.conjunction();

            if (productId != null)
                predicates = cb.and(predicates, cb.equal(root.get("product").get("id"), productId));
            if (userId != null)
                predicates = cb.and(predicates, cb.equal(root.get("user").get("id"), userId));
            if (minRating != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("rating"), minRating));
            if (maxRating != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("rating"), maxRating));
            if (commentKeyword != null && !commentKeyword.isEmpty())
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("comment")), "%" + commentKeyword.toLowerCase() + "%"));
            if (createdFrom != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            if (createdTo != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            if (updatedFrom != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom));
            if (updatedTo != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo));

            return predicates;
        };
    }
}
