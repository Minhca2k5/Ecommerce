package com.minzetsu.ecommerce.promotion.repository;

import com.minzetsu.ecommerce.promotion.dto.filter.VoucherFilter;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import org.springframework.data.jpa.domain.Specification;

public class VoucherSpecification {
    public static Specification<Voucher> filter(VoucherFilter filter) {
        String name = filter.getName();
        String code = filter.getCode();
        String discountType = filter.getDiscountType();
        String status = filter.getStatus();

        Double discountValueFrom = filter.getDiscountValueFrom();
        Double discountValueTo = filter.getDiscountValueTo();

        Double minOrderTotalFrom = filter.getMinOrderTotalFrom();
        Double minOrderTotalTo = filter.getMinOrderTotalTo();

        String startAtFrom = filter.getStartAtFrom();
        String startAtTo = filter.getStartAtTo();

        String endAtFrom = filter.getEndAtFrom();
        String endAtTo = filter.getEndAtTo();

        Integer usageLimitGlobal = filter.getUsageLimitGlobal();
        Integer usageLimitUser = filter.getUsageLimitUser();

        return (root, query, cb) -> {
            query.distinct(true);
            var predicates = cb.conjunction();
            if (name != null && !name.isEmpty()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (code != null && !code.isEmpty()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }
            if (discountType != null && !discountType.isEmpty()) {
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("discountType")), discountType.toLowerCase()));
            }
            if (status != null && !status.isEmpty()) {
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }
            if (discountValueFrom != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("discountValue"), discountValueFrom));
            }
            if (discountValueTo != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("discountValue"), discountValueTo));
            }
            if (minOrderTotalFrom != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("minOrderTotal"), minOrderTotalFrom));
            }
            if (minOrderTotalTo != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("minOrderTotal"), minOrderTotalTo));
            }
            if (startAtFrom != null && !startAtFrom.isEmpty()) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("startAt"), startAtFrom));
            }
            if (startAtTo != null && !startAtTo.isEmpty()) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("startAt"), startAtTo));
            }
            if (endAtFrom != null && !endAtFrom.isEmpty()) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("endAt"), endAtFrom));
            }
            if (endAtTo != null && !endAtTo.isEmpty()) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("endAt"), endAtTo));
            }
            if (usageLimitGlobal != null) {
                predicates = cb.and(predicates, cb.equal(root.get("usageLimitGlobal"), usageLimitGlobal));
            }
            if (usageLimitUser != null) {
                predicates = cb.and(predicates, cb.equal(root.get("usageLimitUser"), usageLimitUser));
            }

            return predicates;
        };
    }
}
