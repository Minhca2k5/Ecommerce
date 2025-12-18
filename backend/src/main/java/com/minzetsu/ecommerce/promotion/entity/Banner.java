package com.minzetsu.ecommerce.promotion.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Column(name = "target_url", length = 512)
    private String targetUrl;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Boolean isActive;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}
