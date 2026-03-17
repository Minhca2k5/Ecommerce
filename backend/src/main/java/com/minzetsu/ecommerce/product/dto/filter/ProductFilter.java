package com.minzetsu.ecommerce.product.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilter implements SortableFilter {
    private Long categoryId;
    private String name;
    private String slug;
    private String sku;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String warehouseLocation;
    private String status;

    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime updatedFrom;
    private LocalDateTime updatedTo;

    private String sortBy;
    private String sortDirection;
}
