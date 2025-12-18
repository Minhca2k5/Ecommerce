package com.minzetsu.ecommerce.user.repository;

import com.minzetsu.ecommerce.user.dto.filter.AddressFilter;
import com.minzetsu.ecommerce.user.entity.Address;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AddressSpecification {

    public static Specification<Address> filter(AddressFilter filter) {

        Long userId = filter.getUserId();
        String line1 = filter.getLine1();
        String line2 = filter.getLine2();
        String city = filter.getCity();
        String state = filter.getState();
        String country = filter.getCountry();

        LocalDateTime createdFrom = filter.getCreatedFrom();
        LocalDateTime createdTo = filter.getCreatedTo();
        LocalDateTime updatedFrom = filter.getUpdatedFrom();
        LocalDateTime updatedTo = filter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            var predicates = cb.conjunction();

            if (userId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("user").get("id"), userId));
            }
            if (line1 != null && !line1.isEmpty()) {
                Predicate line1Match = cb.like(cb.lower(root.get("line1")), "%" + line1.toLowerCase() + "%");
                Predicate line2Match = cb.like(cb.lower(root.get("line2")), "%" + line1.toLowerCase() + "%");
                predicates = cb.and(predicates, cb.or(line1Match, line2Match));
            }
            if (city != null && !city.isEmpty()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
            }
            if (state != null && !state.isEmpty()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("state")), "%" + state.toLowerCase() + "%"));
            }
            if (country != null && !country.isEmpty()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("country")), "%" + country.toLowerCase() + "%"));
            }

            if (createdFrom != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }
            if (updatedFrom != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom));
            }
            if (updatedTo != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo));
            }

            return predicates;
        };
    }
}
