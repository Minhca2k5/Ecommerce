package com.minzetsu.ecommerce.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO dùng khi tạo hoặc cập nhật tồn kho cho 1 sản phẩm trong 1 kho.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @Min(value = 0, message = "Stock quantity must be non-negative")
    private Integer stockQty;

    @Min(value = 0, message = "Reserved quantity must be non-negative")
    private Integer reservedQty;
}
