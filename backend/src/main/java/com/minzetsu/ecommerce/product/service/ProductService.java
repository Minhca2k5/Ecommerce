package com.minzetsu.ecommerce.product.service;

import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.dto.request.ProductCreateRequest;
import com.minzetsu.ecommerce.product.dto.request.ProductUpdateRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminProductResponse;
import com.minzetsu.ecommerce.product.dto.response.FlashSaleResponse;
import com.minzetsu.ecommerce.product.dto.response.ProductResponse;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    void deleteProduct(Long id);

    Product getProductById(Long id);

    void save(Product product);

    boolean existsById(Long id);

    void updateProductStatus(ProductStatus status, Long id);

    AdminProductResponse createAdminProductResponse(ProductCreateRequest request);

    AdminProductResponse updateAdminProductResponse(ProductUpdateRequest request, Long id);

    AdminProductResponse getFullAdminProductResponseById(Long id);

    Page<AdminProductResponse> searchAdminProductResponses(ProductFilter filter, Pageable pageable);

    Page<ProductResponse> searchProductResponses(ProductFilter filter, Pageable pageable);

    ProductResponse getFullProductResponseById(Long id);

    ProductResponse getFullProductResponseBySlug(String slug);

    List<ProductResponse> getTopRatingProductResponses(Integer days, Integer limit);

    List<ProductResponse> getMostFavoriteProductResponses(Integer days, Integer limit);

    List<ProductResponse> getMostViewedProductResponses(Integer days, Integer limit);

    List<ProductResponse> getBestSellingProductResponses(Integer days, Integer limit);

    FlashSaleResponse getFlashSale(Integer days, Integer limit);
}
