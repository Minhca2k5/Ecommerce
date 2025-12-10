package com.minzetsu.ecommerce.inventory.mapper;

import com.minzetsu.ecommerce.inventory.dto.request.WarehouseCreateRequest;
import com.minzetsu.ecommerce.inventory.dto.request.WarehouseUpdateRequest;
import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
import com.minzetsu.ecommerce.inventory.dto.response.WarehouseResponse;
import com.minzetsu.ecommerce.inventory.entity.Warehouse;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface WarehouseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Warehouse toEntity(WarehouseCreateRequest request);

    @Mapping(target = "inventories", ignore = true)
    WarehouseResponse toResponse(Warehouse warehouse);

    List<WarehouseResponse> toResponseList(List<Warehouse> warehouses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(WarehouseUpdateRequest request, @MappingTarget Warehouse warehouse);

    default WarehouseResponse toFullResponse(
            Warehouse warehouse,
            List<InventoryResponse> inventories
    ) {
        WarehouseResponse response = toResponse(warehouse);
        response.setInventories(inventories);
        return response;
    }
}
