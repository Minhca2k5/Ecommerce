package com.minzetsu.ecommerce.order.repository;

import com.minzetsu.ecommerce.order.dto.filter.OrderFilter;
import com.minzetsu.ecommerce.order.entity.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSpecification {

    public static Specification<Order> filter(OrderFilter filter) {
        Long userId = filter.getUserId();
        String status = filter.getStatus();
        String currency = filter.getCurrency();
        BigDecimal minTotalAmount = filter.getMinTotalAmount();
        BigDecimal maxTotalAmount = filter.getMaxTotalAmount();
        LocalDateTime createdFrom = filter.getCreatedFrom();
        LocalDateTime createdTo = filter.getCreatedTo();
        LocalDateTime updatedFrom = filter.getUpdatedFrom();
        LocalDateTime updatedTo = filter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            Predicate predicates = cb.conjunction();

            if (userId != null)
                predicates = cb.and(predicates, cb.equal(root.get("user").get("id"), userId));
            if (status != null && !status.isEmpty())
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            if (currency != null && !currency.isEmpty())
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("currency")), currency.toLowerCase()));
            if (minTotalAmount != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("totalAmount"), minTotalAmount));
            if (maxTotalAmount != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("totalAmount"), maxTotalAmount));
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
