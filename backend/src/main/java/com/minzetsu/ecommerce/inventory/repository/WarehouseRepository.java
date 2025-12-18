package com.minzetsu.ecommerce.inventory.repository;

import com.minzetsu.ecommerce.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, JpaSpecificationExecutor<Warehouse> {
    @Modifying
    @Query("UPDATE Warehouse w SET w.isActive = :isActive WHERE w.id = :id")
    void updateByIsActiveAndId(Boolean isActive, Long id);
    boolean existsById(Long id);
}
