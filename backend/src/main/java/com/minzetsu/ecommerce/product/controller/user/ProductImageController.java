package com.minzetsu.ecommerce.product.controller.user;

import com.minzetsu.ecommerce.product.dto.response.UserProductImageResponse;
import com.minzetsu.ecommerce.product.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/products/{productId}/images")
@RequiredArgsConstructor
@Tag(name = "User - Product Images", description = "Xem hình ảnh sản phẩm dành cho người dùng")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Operation(
            summary = "Lấy tất cả hình ảnh của sản phẩm",
            description = "Trả về danh sách các hình ảnh (bao gồm ảnh chính và ảnh phụ) của sản phẩm được chỉ định."
    )
    @GetMapping
    public ResponseEntity<List<UserProductImageResponse>> getImagesByProductId(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(productImageService.getUserImagesResponseByProductId(productId));
    }

    @Operation(
            summary = "Lấy hình ảnh chính của sản phẩm",
            description = "Trả về hình ảnh chính (primary) của sản phẩm. Nếu sản phẩm chưa có ảnh chính, trả về null hoặc 404."
    )
    @GetMapping("/primary")
    public ResponseEntity<UserProductImageResponse> getPrimaryImageByProductId(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(productImageService.getPrimaryUserImageResponseByProductId(productId));
    }

    @Operation(
            summary = "Lấy hình ảnh sản phẩm theo ID",
            description = "Trả về thông tin chi tiết của một hình ảnh cụ thể dựa trên ID hình ảnh."
    )
    @GetMapping("/{imageId}")
    public ResponseEntity<UserProductImageResponse> getImageById(
            @PathVariable("imageId") Long imageId
    ) {
        return ResponseEntity.ok(productImageService.getUserImageResponseById(imageId));
    }
}
