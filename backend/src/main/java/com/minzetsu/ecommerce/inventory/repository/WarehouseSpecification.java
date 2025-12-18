package com.minzetsu.ecommerce.inventory.repository;

import com.minzetsu.ecommerce.inventory.dto.filter.WarehouseFilter;
import com.minzetsu.ecommerce.inventory.entity.Warehouse;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class WarehouseSpecification {

    public static Specification<Warehouse> filter(WarehouseFilter filter) {
        String code = filter.getCode();
        String name = filter.getName();
        String address = filter.getAddress();
        String city = filter.getCity();
        String state = filter.getState();
        String country = filter.getCountry();
        String zipcode = filter.getZipcode();
        String phone = filter.getPhone();
        Boolean isActive = filter.getIsActive();

        LocalDateTime createdFrom = filter.getCreatedFrom();
        LocalDateTime createdTo = filter.getCreatedTo();
        LocalDateTime updatedFrom = filter.getUpdatedFrom();
        LocalDateTime updatedTo = filter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            var predicates = cb.conjunction();

            if (code != null && !code.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }
            if (name != null && !name.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (address != null && !address.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%"));
            }
            if (city != null && !city.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
            }
            if (state != null && !state.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("state")), "%" + state.toLowerCase() + "%"));
            }
            if (country != null && !country.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("country")), "%" + country.toLowerCase() + "%"));
            }
            if (zipcode != null && !zipcode.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("zipcode")), "%" + zipcode.toLowerCase() + "%"));
            }
            if (phone != null && !phone.isEmpty()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("phone")), "%" + phone.toLowerCase() + "%"));
            }
            if (isActive != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("isActive"), isActive));
            }

            if (createdFrom != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates = cb.and(predicates,
                        cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }
            if (updatedFrom != null) {
                predicates = cb.and(predicates,
                        cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom));
            }
            if (updatedTo != null) {
                predicates = cb.and(predicates,
                        cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo));
            }

            return predicates;
        };
    }
}
