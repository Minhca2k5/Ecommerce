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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Inventories", description = "Quản lý kho hàng và số lượng tồn kho")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @Operation(
            summary = "Tạo mới bản ghi tồn kho",
            description = "Khởi tạo thông tin tồn kho mới cho một sản phẩm tại kho cụ thể."
    )
    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody InventoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createAdminInventoryResponse(request));
    }

    @Operation(
            summary = "Xóa bản ghi tồn kho",
            description = "Xóa thông tin tồn kho dựa trên ID tồn kho."
    )
    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> deleteInventory(@PathVariable("inventoryId") Long inventoryId) {
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Tìm kiếm tồn kho",
            description = "Lọc và tìm kiếm các bản ghi tồn kho dựa trên điều kiện như productId, warehouseId, hoặc khoảng số lượng."
    )
    @GetMapping
    public ResponseEntity<Page<InventoryResponse>> searchInventories(
            @ModelAttribute InventoryFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(inventoryService.searchAdminInventoryResponses(filter, pageable));
    }

    @Operation(
            summary = "Lấy danh sách tồn kho theo sản phẩm",
            description = "Trả về danh sách các bản ghi tồn kho tương ứng với một sản phẩm cụ thể."
    )
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryResponse>> getInventoriesByProductId(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(inventoryService.getAdminInventoryResponsesByProductId(productId));
    }

    @Operation(
            summary = "Lấy danh sách tồn kho theo kho",
            description = "Trả về danh sách các bản ghi tồn kho thuộc về một kho cụ thể."
    )
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponse>> getInventoriesByWarehouseId(
            @PathVariable("warehouseId") Long warehouseId
    ) {
        return ResponseEntity.ok(inventoryService.getAdminInventoryResponsesByWarehouseId(warehouseId));
    }

    @Operation(
            summary = "Cập nhật số lượng tồn thực tế",
            description = "Điều chỉnh trực tiếp số lượng hàng tồn kho (stock quantity) dựa trên ID tồn kho."
    )
    @PatchMapping("/{inventoryId}/stock")
    public ResponseEntity<Void> updateStockQuantityById(
            @PathVariable("inventoryId") Long inventoryId,
            @RequestParam Integer quantity
    ) {
        inventoryService.updateStockQuantityById(quantity, inventoryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Cập nhật số lượng hàng đã đặt (reserved quantity)",
            description = "Cập nhật số lượng hàng đã được đặt giữ trong kho, dựa trên ID tồn kho."
    )
    @PatchMapping("/{inventoryId}/reserved")
    public ResponseEntity<Void> updateReservedQuantityById(
            @PathVariable("inventoryId") Long inventoryId,
            @RequestParam Integer quantity
    ) {
        inventoryService.updateReservedQuantityById(quantity, inventoryId);
        return ResponseEntity.noContent().build();
    }
}
