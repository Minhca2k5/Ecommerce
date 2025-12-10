package com.minzetsu.ecommerce.activity.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "recent_views",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_recent_view_user_product", columnNames = {"user_id", "product_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentView extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
