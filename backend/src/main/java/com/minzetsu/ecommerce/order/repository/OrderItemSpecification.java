package com.minzetsu.ecommerce.order.repository;

import com.minzetsu.ecommerce.order.dto.filter.OrderItemFilter;
import com.minzetsu.ecommerce.order.entity.OrderItem;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class OrderItemSpecification {

    public static Specification<OrderItem> filter(OrderItemFilter filter) {
        Long orderId = filter.getOrderId();
        Long productId = filter.getProductId();
        String productName = filter.getProductNameSnapshot();
        Integer minQuantity = filter.getMinQuantity();
        Integer maxQuantity = filter.getMaxQuantity();
        var minLineTotal = filter.getMinLineTotal();
        var maxLineTotal = filter.getMaxLineTotal();
        var createdFrom = filter.getCreatedFrom();
        var createdTo = filter.getCreatedTo();
        var updatedFrom = filter.getUpdatedFrom();
        var updatedTo = filter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            Predicate predicates = cb.conjunction();

            if (orderId != null)
                predicates = cb.and(predicates, cb.equal(root.get("order").get("id"), orderId));
            if (productId != null)
                predicates = cb.and(predicates, cb.equal(root.get("product").get("id"), productId));
            if (productName != null && !productName.isEmpty())
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("productNameSnapshot")), "%" + productName.toLowerCase() + "%"));
            if (minQuantity != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("quantity"), minQuantity));
            if (maxQuantity != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("quantity"), maxQuantity));
            if (minLineTotal != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("lineTotal"), minLineTotal));
            if (maxLineTotal != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("lineTotal"), maxLineTotal));
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
