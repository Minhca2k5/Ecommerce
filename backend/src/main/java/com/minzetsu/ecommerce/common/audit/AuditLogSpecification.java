package com.minzetsu.ecommerce.common.audit;

import com.minzetsu.ecommerce.common.audit.dto.filter.AuditLogFilter;
import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecification {
    private AuditLogSpecification() {
    }

    public static Specification<AuditLog> filter(AuditLogFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            var predicates = cb.conjunction();

            if (filter.getUserId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("userId"), filter.getUserId()));
            }
            if (filter.getEntityId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("entityId"), filter.getEntityId()));
            }
            if (filter.getSuccess() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("success"), filter.getSuccess()));
            }
            if (filter.getAction() != null && !filter.getAction().isBlank()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("action")), "%" + filter.getAction().toLowerCase() + "%"));
            }
            if (filter.getEntityType() != null && !filter.getEntityType().isBlank()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("entityType")), "%" + filter.getEntityType().toLowerCase() + "%"));
            }
            if (filter.getFrom() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFrom()));
            }
            if (filter.getTo() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), filter.getTo()));
            }
            return predicates;
        };
    }
}
