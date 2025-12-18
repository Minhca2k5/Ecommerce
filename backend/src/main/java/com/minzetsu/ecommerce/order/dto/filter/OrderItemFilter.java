package com.minzetsu.ecommerce.order.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemFilter implements SortableFilter {
    private Long orderId;
    private Long productId;
    private String productNameSnapshot;
    private Integer minQuantity;
    private Integer maxQuantity;
    private BigDecimal minLineTotal;
    private BigDecimal maxLineTotal;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime updatedFrom;
    private LocalDateTime updatedTo;

    private String sortBy;
    private String sortDirection;
}
