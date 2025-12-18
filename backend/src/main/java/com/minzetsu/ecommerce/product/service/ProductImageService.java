package com.minzetsu.ecommerce.product.service;

import com.minzetsu.ecommerce.product.dto.request.ProductImageRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminProductImageResponse;
import com.minzetsu.ecommerce.product.dto.response.ProductImageResponse;
import com.minzetsu.ecommerce.product.entity.ProductImage;

import java.util.List;

public interface ProductImageService {
    void deleteImageById(Long id);
    ProductImage getPrimaryImageByProductId(Long productId);
    void updateImageUrlById(String url, Long id);
    void updateImageIsPrimaryById(Long id, Long productId);
    boolean existsByProductId(Long productId);
    boolean existsById(Long id);
    AdminProductImageResponse createAdminProductImageResponse(ProductImageRequest request);
    List<AdminProductImageResponse> getAdminImagesResponseByProductId(Long productId);
    AdminProductImageResponse getPrimaryAdminImageResponseByProductId(Long productId);
    AdminProductImageResponse getAdminImageResponseById(Long id);

    List<ProductImageResponse> getImagesResponseByProductId(Long productId);
    ProductImageResponse getPrimaryImageResponseByProductId(Long productId);
    ProductImageResponse getImageResponseById(Long id);
}
