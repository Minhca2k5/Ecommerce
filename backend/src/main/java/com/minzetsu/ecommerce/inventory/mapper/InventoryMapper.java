package com.minzetsu.ecommerce.inventory.mapper;

import com.minzetsu.ecommerce.inventory.dto.request.InventoryRequest;
import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
import com.minzetsu.ecommerce.inventory.entity.Inventory;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper để chuyển đổi giữa Inventory entity ↔ DTO.
 */
@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface InventoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    Inventory toEntity(InventoryRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseCode", source = "warehouse.code")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "isActive", source = "warehouse.isActive")
    InventoryResponse toAdminResponse(Inventory inventory);

    List<InventoryResponse> toAdminResponseList(List<Inventory> inventories);
}
