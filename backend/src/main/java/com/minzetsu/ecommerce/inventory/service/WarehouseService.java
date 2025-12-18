package com.minzetsu.ecommerce.inventory.service;

import com.minzetsu.ecommerce.inventory.dto.filter.WarehouseFilter;
import com.minzetsu.ecommerce.inventory.dto.request.WarehouseCreateRequest;
import com.minzetsu.ecommerce.inventory.dto.request.WarehouseUpdateRequest;
import com.minzetsu.ecommerce.inventory.dto.response.WarehouseResponse;
import com.minzetsu.ecommerce.inventory.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {
    void updateIsActiveAndId(boolean isActive, Long id);
    void deleteWarehouse(Long id);
    boolean existsById(Long id);
    Warehouse getWarehouseById(Long id);

    Page<WarehouseResponse> searchWarehouseResponses(WarehouseFilter filter, Pageable pageable);
    WarehouseResponse getWarehouseResponseById(Long id);
    WarehouseResponse getFullWarehouseResponseById(Long id);
    WarehouseResponse createWarehouseResponse(WarehouseCreateRequest request);
    WarehouseResponse updateWarehouseResponse(WarehouseUpdateRequest request, Long id);
}
