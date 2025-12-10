package com.minzetsu.ecommerce.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryResponse extends BaseDTO {

    private Long productId;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Integer stockQty;
    private Integer reservedQty;
    private Boolean isActive;
}
