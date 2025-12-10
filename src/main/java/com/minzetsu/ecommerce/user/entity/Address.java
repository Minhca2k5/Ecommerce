package com.minzetsu.ecommerce.user.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {

    @Column(length = 255)
    private String line1;

    @Column(length = 255)
    private String line2;

    @Column(length = 128)
    private String city;

    @Column(length = 128)
    private String state;

    @Column(length = 64)
    private String country;

    @Column(length = 32)
    private String zipcode;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
