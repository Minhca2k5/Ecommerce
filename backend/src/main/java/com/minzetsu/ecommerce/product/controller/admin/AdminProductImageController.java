package com.minzetsu.ecommerce.product.controller.admin;

import com.minzetsu.ecommerce.product.dto.request.ProductImageRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminProductImageResponse;
import com.minzetsu.ecommerce.product.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/product-images")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Product Images", description = "Quản lý hình ảnh sản phẩm (thêm, xóa, cập nhật, tìm kiếm)")
public class AdminProductImageController {

    private final ProductImageService productImageService;

    @Operation(
            summary = "Tạo hình ảnh sản phẩm mới",
            description = "Thêm một hình ảnh mới cho sản phẩm. Có thể gắn cờ 'primary' nếu là ảnh chính."
    )
    @PostMapping
    public ResponseEntity<AdminProductImageResponse> createProductImage(
            @Valid @RequestBody ProductImageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productImageService.createAdminProductImageResponse(request));
    }

    @Operation(
            summary = "Xóa hình ảnh sản phẩm",
            description = "Xóa hình ảnh của sản phẩm dựa trên ID hình ảnh."
    )
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable("imageId") Long imageId
    ) {
        productImageService.deleteImageById(imageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Cập nhật URL hình ảnh",
            description = "Cập nhật đường dẫn (URL) của hình ảnh sản phẩm dựa trên ID hình ảnh."
    )
    @PatchMapping("/{imageId}/url")
    public ResponseEntity<Void> updateImageUrl(
            @PathVariable("imageId") Long imageId,
            @RequestParam String url
    ) {
        productImageService.updateImageUrlById(url, imageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Cập nhật ảnh chính cho sản phẩm",
            description = "Đặt một hình ảnh làm ảnh chính cho sản phẩm dựa trên ID hình ảnh và ID sản phẩm."
    )
    @PatchMapping("/{imageId}/primary")
    public ResponseEntity<Void> updateImagePrimary(
            @PathVariable("imageId") Long imageId,
            @RequestParam Long productId
    ) {
        productImageService.updateImageIsPrimaryById(imageId, productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lấy danh sách hình ảnh theo sản phẩm",
            description = "Trả về tất cả hình ảnh của một sản phẩm cụ thể (không phân biệt ảnh chính hay phụ)."
    )
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<AdminProductImageResponse>> getImagesByProductId(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(productImageService.getAdminImagesResponseByProductId(productId));
    }

    @Operation(
            summary = "Lấy hình ảnh chính của sản phẩm",
            description = "Trả về hình ảnh được đánh dấu là ảnh chính (primary) của một sản phẩm cụ thể."
    )
    @GetMapping("/product/{productId}/primary")
    public ResponseEntity<AdminProductImageResponse> getPrimaryImageByProductId(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(productImageService.getPrimaryAdminImageResponseByProductId(productId));
    }

    @Operation(
            summary = "Lấy thông tin hình ảnh theo ID",
            description = "Trả về thông tin chi tiết của một hình ảnh sản phẩm dựa trên ID hình ảnh."
    )
    @GetMapping("/{imageId}")
    public ResponseEntity<AdminProductImageResponse> getImageById(
            @PathVariable("imageId") Long imageId
    ) {
        return ResponseEntity.ok(productImageService.getAdminImageResponseById(imageId));
    }
}
