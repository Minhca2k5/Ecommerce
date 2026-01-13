package com.minzetsu.ecommerce.inventory.service;

import com.minzetsu.ecommerce.inventory.dto.filter.InventoryFilter;
import com.minzetsu.ecommerce.inventory.dto.request.InventoryRequest;
import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
import com.minzetsu.ecommerce.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface InventoryService {
    void deleteInventory(Long id);
    List<Inventory> getInventoriesByProductId(Long productId);
    List<Inventory> getActiveInventoriesByProductId(Long productId);
    List<Inventory> getActiveInventoriesByProductIdAndAvailableStock(Long productId);
    List<Inventory> getActiveInventoriesByProductIdAndReservedStock(Long productId);
    List<Inventory> getInventoriesByWarehouseId(Long warehouseId);
    boolean existsByProductId(Long productId);
    boolean existsByWarehouseId(Long warehouseId);
    boolean existsById(Long id);
    Integer getTotalStockQuantityByProductId(Long productId);
    Integer getTotalReservedQuantityByProductId(Long productId);
    Integer getAvailableStockQuantityByProductId(Long productId);
    void updateQuantityByCartItemAmountBorrowed(Long productId, Integer amount);
    void updateQuantityByCartItemAmountReturned(Long productId, Integer amount);
    void updateStockQuantityById(Integer quantity, Long id);
    void updateReservedQuantityById(Integer quantity, Long id);
    List<InventoryResponse> getAdminInventoryResponsesByProductId(Long productId);
    List<InventoryResponse> getAdminInventoryResponsesByWarehouseId(Long warehouseId);
    InventoryResponse createAdminInventoryResponse(InventoryRequest request);
    Page<InventoryResponse> searchAdminInventoryResponses(InventoryFilter filter, Pageable pageable);
}
