package com.minzetsu.ecommerce.inventory.service.impl;

import com.minzetsu.ecommerce.common.audit.entity.AuditAction;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.inventory.dto.filter.WarehouseFilter;
import com.minzetsu.ecommerce.inventory.dto.request.WarehouseCreateRequest;
import com.minzetsu.ecommerce.inventory.dto.request.WarehouseUpdateRequest;
import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
import com.minzetsu.ecommerce.inventory.dto.response.WarehouseResponse;
import com.minzetsu.ecommerce.inventory.entity.Warehouse;
import com.minzetsu.ecommerce.inventory.mapper.InventoryMapper;
import com.minzetsu.ecommerce.inventory.mapper.WarehouseMapper;
import com.minzetsu.ecommerce.inventory.repository.InventoryRepository;
import com.minzetsu.ecommerce.inventory.repository.WarehouseRepository;
import com.minzetsu.ecommerce.inventory.repository.WarehouseSpecification;
import com.minzetsu.ecommerce.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    private Warehouse getExistingWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + id));
    }

    @Override
    @Transactional
    @AuditAction(action = "WAREHOUSE_STATUS_UPDATED", entityType = "WAREHOUSE", idParamIndex = 1)
    public void updateIsActiveAndId(boolean isActive, Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Warehouse not found with id: " + id);
        }
        warehouseRepository.updateByIsActiveAndId(isActive, id);
    }

    @Override
    @Transactional
    @AuditAction(action = "WAREHOUSE_DELETED", entityType = "WAREHOUSE", idParamIndex = 0)
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = getExistingWarehouse(id);
        inventoryRepository.deleteByWarehouseId(id);
        warehouseRepository.delete(warehouse);
    }

    @Override
    public boolean existsById(Long id) {
        return warehouseRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Warehouse getWarehouseById(Long id) {
        return getExistingWarehouse(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseResponse> searchWarehouseResponses(WarehouseFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                warehouseRepository,
                WarehouseSpecification.filter(filter),
                warehouseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseResponseById(Long id) {
        Warehouse warehouse = getExistingWarehouse(id);
        return warehouseMapper.toResponse(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getFullWarehouseResponseById(Long id) {
        Warehouse warehouse = getExistingWarehouse(id);
        List<InventoryResponse> inventories = inventoryMapper
                .toAdminResponseList(inventoryRepository.findByWarehouseId(id));
        return warehouseMapper.toFullResponse(warehouse, inventories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPublicWarehouseLocations() {
        return warehouseRepository.findDistinctActiveCities();
    }

    @Override
    @Transactional
    @AuditAction(action = "WAREHOUSE_CREATED", entityType = "WAREHOUSE")
    public WarehouseResponse createWarehouseResponse(WarehouseCreateRequest request) {
        Warehouse warehouse = warehouseMapper.toEntity(request);
        return warehouseMapper.toResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    @AuditAction(action = "WAREHOUSE_UPDATED", entityType = "WAREHOUSE", idParamIndex = 1)
    public WarehouseResponse updateWarehouseResponse(WarehouseUpdateRequest request, Long id) {
        Warehouse warehouse = getExistingWarehouse(id);
        warehouseMapper.updateEntityFromRequest(request, warehouse);
        return warehouseMapper.toResponse(warehouseRepository.save(warehouse));
    }
}
