package com.minzetsu.ecommerce.inventory.repository;

import com.minzetsu.ecommerce.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, JpaSpecificationExecutor<Warehouse> {
    @Modifying
    @Query("UPDATE Warehouse w SET w.isActive = :isActive WHERE w.id = :id")
    void updateByIsActiveAndId(Boolean isActive, Long id);

    @Query("""
            SELECT DISTINCT w.city
            FROM Warehouse w
            WHERE w.isActive = true
                AND w.city IS NOT NULL
                AND TRIM(w.city) <> ''
            ORDER BY w.city ASC
            """)
    List<String> findDistinctActiveCities();

    boolean existsById(Long id);
}
