package com.minzetsu.ecommerce.product.controller.admin;

import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.dto.request.ProductCreateRequest;
import com.minzetsu.ecommerce.product.dto.request.ProductUpdateRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminProductResponse;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.product.service.ProductService;
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
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới được truy cập
@Tag(name = "Admin - Products", description = "Quản lý sản phẩm (thêm, sửa, xóa, tìm kiếm, cập nhật trạng thái)")
public class AdminProductController {

    private final ProductService productService;

    @Operation(
            summary = "Tạo sản phẩm mới",
            description = "Thêm một sản phẩm mới vào hệ thống. Bao gồm thông tin cơ bản và danh mục liên kết."
    )
    @PostMapping
    public ResponseEntity<AdminProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createAdminProductResponse(request));
    }

    @Operation(
            summary = "Cập nhật thông tin sản phẩm",
            description = "Cập nhật thông tin của sản phẩm dựa trên ID. Cho phép chỉnh sửa tên, mô tả, giá, hoặc danh mục."
    )
    @PutMapping("/{productId}")
    public ResponseEntity<AdminProductResponse> updateProduct(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.updateAdminProductResponse(request, productId));
    }

    @Operation(
            summary = "Xóa sản phẩm",
            description = "Xóa sản phẩm dựa trên ID. Có thể bao gồm kiểm tra ràng buộc với tồn kho hoặc đơn hàng."
    )
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("productId") Long productId
    ) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lấy thông tin chi tiết sản phẩm theo ID",
            description = "Trả về toàn bộ thông tin chi tiết của sản phẩm, bao gồm ảnh, danh mục, tồn kho, và các thuộc tính khác."
    )
    @GetMapping("/{productId}")
    public ResponseEntity<AdminProductResponse> getProductById(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(productService.getFullAdminProductResponseById(productId));
    }

    @Operation(
            summary = "Tìm kiếm sản phẩm",
            description = "Tìm kiếm sản phẩm theo tên, trạng thái, danh mục, hoặc khoảng giá. Hỗ trợ phân trang và sắp xếp."
    )
    @GetMapping
    public ResponseEntity<Page<AdminProductResponse>> searchProducts(
            @ModelAttribute ProductFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(productService.searchAdminProductResponses(filter, pageable));
    }

    @Operation(
            summary = "Cập nhật trạng thái sản phẩm",
            description = "Cập nhật trạng thái hoạt động của sản phẩm (ACTIVE, INACTIVE, OUT_OF_STOCK, v.v.)."
    )
    @PutMapping("/{productId}/status")
    public ResponseEntity<Void> updateProductStatus(
            @PathVariable("productId") Long productId,
            @RequestParam ProductStatus status
    ) {
        productService.updateProductStatus(status, productId);
        return ResponseEntity.noContent().build();
    }

}
