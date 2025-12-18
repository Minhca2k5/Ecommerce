package com.minzetsu.ecommerce.inventory.controller.admin;

import com.minzetsu.ecommerce.inventory.dto.filter.WarehouseFilter;
import com.minzetsu.ecommerce.inventory.dto.request.WarehouseCreateRequest;
import com.minzetsu.ecommerce.inventory.dto.request.WarehouseUpdateRequest;
import com.minzetsu.ecommerce.inventory.dto.response.WarehouseResponse;
import com.minzetsu.ecommerce.inventory.service.WarehouseService;
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

@RestController
@RequestMapping("/api/admin/warehouses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Warehouses", description = "Quản lý thông tin và trạng thái hoạt động của kho hàng")
public class AdminWarehouseController {

    private final WarehouseService warehouseService;

    @Operation(
            summary = "Tạo kho hàng mới",
            description = "Khởi tạo một kho hàng mới với thông tin cơ bản như tên, địa chỉ, và trạng thái hoạt động."
    )
    @PostMapping
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @Valid @RequestBody WarehouseCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(warehouseService.createWarehouseResponse(request));
    }

    @Operation(
            summary = "Cập nhật thông tin kho hàng",
            description = "Cập nhật tên, địa chỉ hoặc thông tin khác của kho hàng dựa trên ID."
    )
    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponse> updateWarehouseById(
            @PathVariable("warehouseId") Long warehouseId,
            @Valid @RequestBody WarehouseUpdateRequest request
    ) {
        return ResponseEntity.ok(warehouseService.updateWarehouseResponse(request, warehouseId));
    }

    @Operation(
            summary = "Xóa kho hàng",
            description = "Xóa một kho hàng khỏi hệ thống dựa trên ID."
    )
    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Void> deleteWarehouseById(@PathVariable("warehouseId") Long warehouseId) {
        warehouseService.deleteWarehouse(warehouseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Cập nhật trạng thái hoạt động của kho hàng",
            description = "Kích hoạt hoặc vô hiệu hóa kho hàng (isActive = true/false)."
    )
    @PatchMapping("/{warehouseId}/status")
    public ResponseEntity<Void> updateWarehouseStatus(
            @PathVariable("warehouseId") Long warehouseId,
            @RequestParam boolean active
    ) {
        warehouseService.updateIsActiveAndId(active, warehouseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lấy thông tin kho hàng theo ID",
            description = "Trả về thông tin cơ bản của kho hàng."
    )
    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable("warehouseId") Long warehouseId) {
        return ResponseEntity.ok(warehouseService.getWarehouseResponseById(warehouseId));
    }

    @Operation(
            summary = "Lấy thông tin chi tiết kho hàng",
            description = "Trả về thông tin đầy đủ của kho hàng, bao gồm danh sách hàng tồn và chi tiết liên quan."
    )
    @GetMapping("/{warehouseId}/details")
    public ResponseEntity<WarehouseResponse> getWarehouseDetailsById(@PathVariable("warehouseId") Long warehouseId) {
        return ResponseEntity.ok(warehouseService.getFullWarehouseResponseById(warehouseId));
    }

    @Operation(
            summary = "Tìm kiếm và lọc kho hàng",
            description = "Lọc kho hàng theo tên, trạng thái, hoặc các tiêu chí khác. Hỗ trợ phân trang."
    )
    @GetMapping
    public ResponseEntity<Page<WarehouseResponse>> searchWarehouses(
            @ModelAttribute WarehouseFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(warehouseService.searchWarehouseResponses(filter, pageable));
    }
}
