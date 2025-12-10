package com.minzetsu.ecommerce.notification.repository;

import com.minzetsu.ecommerce.notification.dto.filter.NotificationFilter;
import com.minzetsu.ecommerce.notification.entity.Notification;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationSpecification {

    public static Specification<Notification> filter(NotificationFilter filter) {
        Boolean isRead = filter.getIsRead();
        Boolean isHidden = filter.getIsHidden();
        List<String> types = filter.getTypes();
        String referenceType = filter.getReferenceType();
        LocalDateTime fromDate = filter.getFromDate();
        LocalDateTime toDate = filter.getToDate();
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (isRead != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("isRead"), isRead));
            }

            if (isHidden != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("isHidden"), isHidden));
            }

            if (types != null && !types.isEmpty()) {
                predicates = cb.and(predicates,
                        root.get("type").in(types));
            }

            if (referenceType != null && !referenceType.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("referenceType"), referenceType));
            }

            if (fromDate != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates = cb.and(predicates,
                        cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            return predicates;
        };
    }
}
