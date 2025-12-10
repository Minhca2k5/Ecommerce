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
public class OrderFilter implements SortableFilter {
    private Long userId;
    private String status;
    private String currency;
    private BigDecimal minTotalAmount;
    private BigDecimal maxTotalAmount;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime updatedFrom;
    private LocalDateTime updatedTo;

    private String sortBy;
    private String sortDirection;
}
