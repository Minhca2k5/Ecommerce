package com.minzetsu.ecommerce.payment.repository;

import com.minzetsu.ecommerce.payment.dto.filter.PaymentFilter;
import com.minzetsu.ecommerce.payment.entity.Payment;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PaymentSpecification {

    public static Specification<Payment> filter(PaymentFilter filter) {
        Long orderId = filter.getOrderId();
        String status = filter.getStatus();
        String method = filter.getMethod();
        String currency = filter.getCurrency();
        var minAmount = filter.getMinAmount();
        var maxAmount = filter.getMaxAmount();
        var createdFrom = filter.getCreatedFrom();
        var createdTo = filter.getCreatedTo();
        var updatedFrom = filter.getUpdatedFrom();
        var updatedTo = filter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            Predicate predicates = cb.conjunction();

            if (orderId != null)
                predicates = cb.and(predicates, cb.equal(root.get("order").get("id"), orderId));
            if (status != null && !status.isEmpty())
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            if (method != null && !method.isEmpty())
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("method")), method.toLowerCase()));
            if (currency != null && !currency.isEmpty())
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("currency")), currency.toLowerCase()));
            if (minAmount != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            if (maxAmount != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
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
