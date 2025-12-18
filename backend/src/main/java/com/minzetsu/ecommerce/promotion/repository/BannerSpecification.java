package com.minzetsu.ecommerce.promotion.repository;

import com.minzetsu.ecommerce.promotion.dto.filter.BannerFilter;
import com.minzetsu.ecommerce.promotion.entity.Banner;
import org.springframework.data.jpa.domain.Specification;

public class BannerSpecification {
    public static Specification<Banner> filter(BannerFilter filter) {
        String title = filter.getTitle();
        Boolean isActive = filter.getIsActive();
        Integer position = filter.getPosition();
        String startAtFrom = filter.getStartAtFrom();
        String startAtTo = filter.getStartAtTo();
        String endAtFrom = filter.getEndAtFrom();
        String endAtTo = filter.getEndAtTo();
        return (root, query, db) -> {
            query.distinct(true);
            var predicates = db.conjunction();
            if (title != null && !title.isEmpty()) {
                predicates = db.and(predicates, db.like(db.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (isActive != null) {
                predicates = db.and(predicates, db.equal(root.get("isActive"), isActive));
            }
            if (position != null) {
                predicates = db.and(predicates, db.equal(root.get("position"), position));
            }
            if (startAtFrom != null && !startAtFrom.isEmpty()) {
                predicates = db.and(predicates, db.greaterThanOrEqualTo(root.get("startAt"), startAtFrom));
            }
            if (startAtTo != null && !startAtTo.isEmpty()) {
                predicates = db.and(predicates, db.lessThanOrEqualTo(root.get("startAt"), startAtTo));
            }
            if (endAtFrom != null && !endAtFrom.isEmpty()) {
                predicates = db.and(predicates, db.greaterThanOrEqualTo(root.get("endAt"), endAtFrom));
            }
            if (endAtTo != null && !endAtTo.isEmpty()) {
                predicates = db.and(predicates, db.lessThanOrEqualTo(root.get("endAt"), endAtTo));
            }
            return predicates;
        };
    }
}
