package com.minzetsu.ecommerce.product.service.impl;

import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.product.dto.request.ProductImageRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminProductImageResponse;
import com.minzetsu.ecommerce.product.dto.response.ProductImageResponse;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.entity.ProductImage;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.product.mapper.ProductImageMapper;
import com.minzetsu.ecommerce.product.repository.ProductImageRepository;
import com.minzetsu.ecommerce.product.service.ProductImageService;
import com.minzetsu.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductImageMapper productImageMapper;
    private final ProductService productService;

    private ProductImage getExistingImage(Long id) {
        return productImageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found with id: " + id));
    }

    private void validateActiveProduct(Product product, Long productId) {
        if (!ProductStatus.ACTIVE.equals(product.getStatus())) {
            throw new UnAuthorizedException("Product is not active with id: " + productId);
        }
    }

    @Override
    @Transactional
    public void deleteImageById(Long id) {
        ProductImage image = getExistingImage(id);
        productImageRepository.delete(image);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImage getPrimaryImageByProductId(Long productId) {
        return productImageRepository.findByIsPrimaryTrueAndProductId(productId)
                .orElseThrow(() -> new NotFoundException("Primary image not found for productId: " + productId));
    }

    @Override
    @Transactional
    public void updateImageUrlById(String url, Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Image not found with id: " + id);
        }
        productImageRepository.updateByUrl(url, id);
    }

    @Override
    @Transactional
    public void updateImageIsPrimaryById(Long id, Long productId) {
        ProductImage productImage = getExistingImage(id);

        if (!productImage.getProduct().getId().equals(productId)) {
            throw new UnAuthorizedException("Image does not belong to the specified product");
        }

        productImageRepository.findByIsPrimaryTrueAndProductId(productId)
                .ifPresent(currentPrimary ->
                        productImageRepository.updateIsPrimaryById(false, currentPrimary.getId()));

        productImageRepository.updateIsPrimaryById(true, id);
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return productImageRepository.existsByProductId(productId);
    }

    @Override
    public boolean existsById(Long id) {
        return productImageRepository.existsById(id);
    }

    @Override
    @Transactional
    public AdminProductImageResponse createAdminProductImageResponse(ProductImageRequest request) {
        Long productId = request.getProductId();
        if (!productService.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }

        Product product = productService.getProductById(productId);
        ProductImage image = productImageMapper.toEntity(request);
        image.setProduct(product);

        Optional<ProductImage> existingPrimary = productImageRepository.findByIsPrimaryTrueAndProductId(productId);
        image.setIsPrimary(existingPrimary.isEmpty());

        ProductImage savedImage = productImageRepository.save(image);
        return productImageMapper.toAdminResponse(savedImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProductImageResponse> getAdminImagesResponseByProductId(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        return productImageMapper.toAdminResponseList(images);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductImageResponse getPrimaryAdminImageResponseByProductId(Long productId) {
        ProductImage primaryImage = productImageRepository.findByIsPrimaryTrueAndProductId(productId)
                .orElseThrow(() -> new NotFoundException("Primary image not found for productId: " + productId));
        return productImageMapper.toAdminResponse(primaryImage);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductImageResponse getAdminImageResponseById(Long id) {
        ProductImage image = getExistingImage(id);
        return productImageMapper.toAdminResponse(image);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getImagesResponseByProductId(Long productId) {
        Product product = productService.getProductById(productId);
        validateActiveProduct(product, productId);

        List<ProductImage> images = productImageRepository.findByProductId(productId);
        return productImageMapper.toResponseList(images);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageResponse getPrimaryImageResponseByProductId(Long productId) {
        Product product = productService.getProductById(productId);
        validateActiveProduct(product, productId);

        ProductImage primaryImage = productImageRepository.findByIsPrimaryTrueAndProductId(productId)
                .orElseThrow(() -> new NotFoundException("Primary image not found for productId: " + productId));
        return productImageMapper.toResponse(primaryImage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageResponse getImageResponseById(Long id) {
        ProductImage image = getExistingImage(id);
        validateActiveProduct(image.getProduct(), image.getProduct().getId());
        return productImageMapper.toResponse(image);
    }
}
