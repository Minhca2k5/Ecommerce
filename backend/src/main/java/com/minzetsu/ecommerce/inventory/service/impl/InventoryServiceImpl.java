package com.minzetsu.ecommerce.inventory.service.impl;

import com.minzetsu.ecommerce.common.audit.AuditAction;
import com.minzetsu.ecommerce.common.exception.InsufficientNumberException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.inventory.dto.filter.InventoryFilter;
import com.minzetsu.ecommerce.inventory.dto.request.InventoryRequest;
import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
import com.minzetsu.ecommerce.inventory.entity.Inventory;
import com.minzetsu.ecommerce.inventory.mapper.InventoryMapper;
import com.minzetsu.ecommerce.inventory.repository.InventoryRepository;
import com.minzetsu.ecommerce.inventory.repository.InventorySpecification;
import com.minzetsu.ecommerce.inventory.service.InventoryService;
import com.minzetsu.ecommerce.inventory.service.WarehouseService;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final ProductService productService;
    private final WarehouseService warehouseService;

    private void validateProductAndWarehouse(Long productId, Long warehouseId) {
        if (!productService.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        if (!warehouseService.existsById(warehouseId)) {
            throw new NotFoundException("Warehouse not found with id: " + warehouseId);
        }
    }

    private List<Inventory> findByCheck(boolean exists, Supplier<List<Inventory>> finder, String message) {
        if (!exists) throw new NotFoundException(message);
        return finder.get();
    }

    @Override
    @Transactional
    @AuditAction(action = "INVENTORY_DELETED", entityType = "INVENTORY", idParamIndex = 0)
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory not found with id: " + id));
        inventoryRepository.delete(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getInventoriesByProductId(Long productId) {
        return findByCheck(existsByProductId(productId),
                () -> inventoryRepository.findByProductId(productId),
                "No Inventories found for productId: " + productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getActiveInventoriesByProductId(Long productId) {
        return findByCheck(existsByProductId(productId),
                () -> inventoryRepository.findActiveByProductId(productId, ProductStatus.ACTIVE),
                "No active Inventories found for productId: " + productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getActiveInventoriesByProductIdAndAvailableStock(Long productId) {
        return findByCheck(existsByProductId(productId),
                () -> inventoryRepository.findActiveByProductIdByAvailableStock(productId, ProductStatus.ACTIVE),
                "No Inventories found for productId: " + productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getActiveInventoriesByProductIdAndReservedStock(Long productId) {
        return findByCheck(existsByProductId(productId),
                () -> inventoryRepository.findActiveByProductIdByReservedStock(productId, ProductStatus.ACTIVE),
                "No Inventories found for productId: " + productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> getInventoriesByWarehouseId(Long warehouseId) {
        return findByCheck(existsByWarehouseId(warehouseId),
                () -> inventoryRepository.findByWarehouseId(warehouseId),
                "No Inventories found for warehouseId: " + warehouseId);
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return inventoryRepository.existsByProductId(productId);
    }

    @Override
    public boolean existsByWarehouseId(Long warehouseId) {
        return inventoryRepository.existsByWarehouseId(warehouseId);
    }

    @Override
    public boolean existsById(Long id) {
        return inventoryRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalStockQuantityByProductId(Long productId) {
        return inventoryRepository.getTotalStockQuantityByProductId(productId, ProductStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalReservedQuantityByProductId(Long productId) {
        return inventoryRepository.getTotalReservedQuantityByProductId(productId, ProductStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAvailableStockQuantityByProductId(Long productId) {
        return inventoryRepository.getAvailableStockQuantityByProductId(productId, ProductStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void updateQuantityByCartItemAmountBorrowed(Long productId, Integer amount) {
        List<Inventory> inventories = getActiveInventoriesByProductIdAndAvailableStock(productId);
        int remaining = amount;
        boolean updated = false;

        for (Inventory inventory : inventories) {
            int available = inventory.getStockQty() - inventory.getReservedQty();
            if (available <= 0) continue;

            int use = Math.min(available, remaining);
            inventoryRepository.updateReservedQuantityById(use, inventory.getId());
            remaining -= use;

            if (remaining == 0) {
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new InsufficientNumberException("Insufficient stock for productId: " + productId);
        }
    }

    @Override
    @Transactional
    public void updateQuantityByCartItemAmountReturnedOrCheckouted(Long productId, Integer amount, boolean isCheckout) {
        List<Inventory> inventories = getActiveInventoriesByProductIdAndReservedStock(productId);
        int remaining = amount;
        boolean updated = false;

        for (Inventory inventory : inventories) {
            int reserved = inventory.getReservedQty();
            if (reserved <= 0) continue;

            int use = Math.min(reserved, remaining);
            if (isCheckout) {
                inventoryRepository.updateStockQuantityById(use, inventory.getId());
            }
            inventoryRepository.updateReservedQuantityById(-use, inventory.getId());
            remaining -= use;

            if (remaining == 0) {
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new InsufficientNumberException("Insufficient reserved stock to return for productId: " + productId);
        }
    }

    @Override
    @Transactional
    @AuditAction(action = "INVENTORY_STOCK_UPDATED", entityType = "INVENTORY", idParamIndex = 1)
    public void updateStockQuantityById(Integer quantity, Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Inventory not found with id: " + id);
        }
        inventoryRepository.updateStockQuantityById(quantity, id);
    }

    @Override
    @Transactional
    @AuditAction(action = "INVENTORY_RESERVED_UPDATED", entityType = "INVENTORY", idParamIndex = 1)
    public void updateReservedQuantityById(Integer quantity, Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Inventory not found with id: " + id);
        }
        inventoryRepository.updateReservedQuantityById(quantity, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAdminInventoryResponsesByProductId(Long productId) {
        return inventoryMapper.toAdminResponseList(getInventoriesByProductId(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAdminInventoryResponsesByWarehouseId(Long warehouseId) {
        return inventoryMapper.toAdminResponseList(getInventoriesByWarehouseId(warehouseId));
    }

    @Override
    @Transactional
    @AuditAction(action = "INVENTORY_CREATED", entityType = "INVENTORY")
    public InventoryResponse createAdminInventoryResponse(InventoryRequest request) {
        Long productId = request.getProductId();
        Long warehouseId = request.getWarehouseId();
        validateProductAndWarehouse(productId, warehouseId);

        Inventory inventory = inventoryMapper.toEntity(request);
        inventory.setWarehouse(warehouseService.getWarehouseById(warehouseId));
        inventory.setProduct(productService.getProductById(productId));

        return inventoryMapper.toAdminResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponse> searchAdminInventoryResponses(InventoryFilter filter, Pageable pageable) {
       return PageableUtils.search(
               filter,
               pageable,
               inventoryRepository,
               InventorySpecification.filter(filter),
               inventoryMapper::toAdminResponse
       );
    }
}
