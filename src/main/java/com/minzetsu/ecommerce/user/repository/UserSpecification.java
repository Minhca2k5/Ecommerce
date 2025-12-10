package com.minzetsu.ecommerce.user.repository;

import com.minzetsu.ecommerce.user.dto.filter.UserFilter;
import com.minzetsu.ecommerce.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filter(UserFilter userFilter) {

        String username = userFilter.getUsername();
        String email = userFilter.getEmail();
        String fullName = userFilter.getFullName();
        String phone = userFilter.getPhone();
        Boolean enabled = userFilter.getEnabled();
        List<String> roleNames = userFilter.getRoleNames();

        LocalDateTime createdFrom = userFilter.getCreatedFrom();
        LocalDateTime createdTo = userFilter.getCreatedTo();
        LocalDateTime updatedFrom = userFilter.getUpdatedFrom();
        LocalDateTime updatedTo = userFilter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
            }
            if (email != null && !email.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (fullName != null && !fullName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%"));
            }
            if (phone != null && !phone.isEmpty()) {
                predicates.add(cb.equal(root.get("phone"), phone));
            }
            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }
            if (roleNames != null && !roleNames.isEmpty()) {
                Join<Object, Object> roleJoin = root.join("roles", JoinType.INNER);
                predicates.add(roleJoin.get("name").in(roleNames));
            }

            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }
            if (updatedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom));
            }
            if (updatedTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
