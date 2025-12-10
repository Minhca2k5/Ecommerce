package com.minzetsu.ecommerce.inventory.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryFilter implements SortableFilter {
    private Long productId;
    private Long warehouseId;
    private Integer minStockQty;
    private Integer maxStockQty;
    private Integer minReservedQty;
    private Integer maxReservedQty;
    private Boolean hasAvailableStock; // true nếu stockQty > reservedQty

    private LocalDateTime updatedFrom;
    private LocalDateTime updatedTo;

    private String sortBy;
    private String sortDirection;
}
