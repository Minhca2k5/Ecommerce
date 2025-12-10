package com.minzetsu.ecommerce.notification.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import com.minzetsu.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private NotificationType type;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(nullable = false)
    private Boolean isRead;

    @Column(nullable = false)
    private Boolean isHidden;
}
