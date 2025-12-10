package com.minzetsu.ecommerce.inventory.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import com.minzetsu.ecommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

/**
 * Inventory entity: lưu tồn kho thực tế của từng sản phẩm trong từng kho.
 * Quan hệ: Nhiều inventory cho 1 product, Nhiều inventory cho 1 warehouse.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "inventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty;

    @Column(name = "reserved_qty", nullable = false)
    private Integer reservedQty;
}
