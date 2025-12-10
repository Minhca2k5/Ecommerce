package com.minzetsu.ecommerce.product.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage extends BaseEntity {

    @Column(nullable = false, length = 512)
    private String url;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
