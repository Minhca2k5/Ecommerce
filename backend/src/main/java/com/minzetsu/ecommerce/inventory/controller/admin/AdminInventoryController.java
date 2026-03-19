package com.minzetsu.ecommerce.inventory.controller.admin;

import com.minzetsu.ecommerce.inventory.dto.filter.InventoryFilter;
import com.minzetsu.ecommerce.inventory.dto.request.InventoryRequest;
import com.minzetsu.ecommerce.inventory.dto.response.InventoryResponse;
import com.minzetsu.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Inventories", description = "Manage warehouses and inventory quantities")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Create inventory", description = "Create a new inventory row for a product in a warehouse.")
    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createAdminInventoryResponse(request));
    }

    @Operation(summary = "Delete inventory", description = "Delete an inventory row by inventory id.")
    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> deleteInventory(@PathVariable("inventoryId") Long inventoryId) {
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search inventories", description = "Filter inventories by product, warehouse, and stock conditions with pagination.")
    @GetMapping
    public ResponseEntity<Page<InventoryResponse>> searchInventories(@ModelAttribute InventoryFilter filter, Pageable pageable) {
        return ResponseEntity.ok(inventoryService.searchAdminInventoryResponses(filter, pageable));
    }

    @Operation(summary = "Get inventories by product", description = "Return inventory rows for a specific product.")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryResponse>> getInventoriesByProductId(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(inventoryService.getAdminInventoryResponsesByProductId(productId));
    }

    @Operation(summary = "Get inventories by warehouse", description = "Return inventory rows for a specific warehouse.")
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponse>> getInventoriesByWarehouseId(@PathVariable("warehouseId") Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getAdminInventoryResponsesByWarehouseId(warehouseId));
    }

    @Operation(
            summary = "Set stock quantity",
            description = "Set the actual stock quantity for an inventory row. Reserved quantity is system-managed."
    )
    @PatchMapping("/{inventoryId}/stock")
    public ResponseEntity<Void> updateStockQuantityById(
            @PathVariable("inventoryId") Long inventoryId,
            @RequestParam Integer quantity
    ) {
        inventoryService.setAdminStockQuantityById(quantity, inventoryId);
        return ResponseEntity.noContent().build();
    }
}
