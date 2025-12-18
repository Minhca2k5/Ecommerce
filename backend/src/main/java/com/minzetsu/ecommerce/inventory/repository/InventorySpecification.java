package com.minzetsu.ecommerce.inventory.repository;

import com.minzetsu.ecommerce.inventory.dto.filter.InventoryFilter;
import com.minzetsu.ecommerce.inventory.entity.Inventory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class InventorySpecification {

    public static Specification<Inventory> filter(InventoryFilter filter) {
        Long productId = filter.getProductId();
        Long warehouseId = filter.getWarehouseId();
        Integer minStockQty = filter.getMinStockQty();
        Integer maxStockQty = filter.getMaxStockQty();
        Integer minReservedQty = filter.getMinReservedQty();
        Integer maxReservedQty = filter.getMaxReservedQty();
        Boolean hasAvailableStock = filter.getHasAvailableStock();
        var updatedFrom = filter.getUpdatedFrom();
        var updatedTo = filter.getUpdatedTo();

        return (root, query, cb) -> {
            query.distinct(true);
            Predicate predicates = cb.conjunction();

            if (productId != null)
                predicates = cb.and(predicates, cb.equal(root.get("product").get("id"), productId));
            if (warehouseId != null)
                predicates = cb.and(predicates, cb.equal(root.get("warehouse").get("id"), warehouseId));
            if (minStockQty != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("stockQty"), minStockQty));
            if (maxStockQty != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("stockQty"), maxStockQty));
            if (minReservedQty != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("reservedQty"), minReservedQty));
            if (maxReservedQty != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("reservedQty"), maxReservedQty));
            if (hasAvailableStock != null && hasAvailableStock)
                predicates = cb.and(predicates,
                        cb.greaterThan(root.get("stockQty"), root.get("reservedQty")));
            if (updatedFrom != null)
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom));
            if (updatedTo != null)
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo));

            return predicates;
        };
    }
}
