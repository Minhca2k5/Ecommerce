package com.minzetsu.ecommerce.user.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

}
