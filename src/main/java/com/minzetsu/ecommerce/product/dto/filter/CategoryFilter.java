package com.minzetsu.ecommerce.product.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryFilter implements SortableFilter {
    private String name;
    private String slug;
    private Long parentId;

    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime updatedFrom;
    private LocalDateTime updatedTo;

    private String sortBy;
    private String sortDirection;
}
