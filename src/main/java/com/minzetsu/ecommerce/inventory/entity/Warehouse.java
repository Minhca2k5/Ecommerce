package com.minzetsu.ecommerce.inventory.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Warehouse entity — mô tả kho hàng vật lý (unidirectional).
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "zipcode", length = 50)
    private String zipcode;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
