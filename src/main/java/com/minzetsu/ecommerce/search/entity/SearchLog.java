package com.minzetsu.ecommerce.search.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import com.minzetsu.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "search_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // nullable = true theo SQL
    private User user;

    @Column(nullable = false, length = 255)
    private String keyword;
}
