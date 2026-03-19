package com.minzetsu.ecommerce.inventory.repository;

import com.minzetsu.ecommerce.inventory.entity.Inventory;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

    List<Inventory> findByProductId(Long productId);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId and i.product.status = :status and i.warehouse.isActive = true")
    List<Inventory> findActiveByProductId(Long productId, ProductStatus status);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId and i.product.status = :status and i.warehouse.isActive = true ORDER BY (i.stockQty - i.reservedQty) DESC")
    List<Inventory> findActiveByProductIdByAvailableStock(Long productId, ProductStatus status);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId and i.product.status = :status and i.warehouse.isActive = true ORDER BY i.reservedQty DESC")
    List<Inventory> findActiveByProductIdByReservedStock(Long productId, ProductStatus status);

    List<Inventory> findByWarehouseId(Long warehouseId);

    @Modifying
    @Query("UPDATE Inventory i SET i.stockQty = i.stockQty - :quantity WHERE i.id = :id")
    void updateStockQuantityById(Integer quantity, Long id);

    @Modifying
    @Query("UPDATE Inventory i SET i.stockQty = :quantity WHERE i.id = :id")
    void setStockQuantityById(Integer quantity, Long id);

    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQty = i.reservedQty + :quantity WHERE i.id = :id")
    void updateReservedQuantityById(Integer quantity, Long id);

    boolean existsByProductId(Long productId);
    boolean existsByWarehouseId(Long warehouseId);
    boolean existsById(Long id);

    @Query("SELECT SUM(i.stockQty) FROM Inventory i WHERE i.product.id = :productId and i.product.status = :status and i.warehouse.isActive = true")
    Integer getTotalStockQuantityByProductId(Long productId, ProductStatus status);

    @Query("SELECT SUM(i.reservedQty) FROM Inventory i WHERE i.product.id = :productId and i.product.status = :status and i.warehouse.isActive = true")
    Integer getTotalReservedQuantityByProductId(Long productId, ProductStatus status);

    @Query("SELECT SUM(i.stockQty - i.reservedQty) FROM Inventory i WHERE i.product.id = :productId and i.product.status = :status and i.warehouse.isActive = true")
    Integer getAvailableStockQuantityByProductId(Long productId, ProductStatus status);

    void deleteByProductId(Long productId);
    void deleteByWarehouseId(Long warehouseId);
}
