package com.minzetsu.ecommerce.review.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewFilter implements SortableFilter {
    private Long productId;
    private Long userId;
    private Integer minRating;
    private Integer maxRating;
    private String commentKeyword;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime updatedFrom;
    private LocalDateTime updatedTo;

    private String sortBy;
    private String sortDirection;
}
