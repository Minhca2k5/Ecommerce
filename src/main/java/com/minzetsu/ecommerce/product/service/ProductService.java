package com.minzetsu.ecommerce.product.service;

import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.dto.request.ProductCreateRequest;
import com.minzetsu.ecommerce.product.dto.request.ProductUpdateRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminProductResponse;
import com.minzetsu.ecommerce.product.dto.response.UserProductResponse;
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

    Page<UserProductResponse> searchUserProductResponses(ProductFilter filter, Pageable pageable);
    UserProductResponse getFullUserProductResponseById(Long id);
    List<UserProductResponse> getTopRatingUserProductResponses(Integer days, Integer limit);
    List<UserProductResponse> getMostFavoriteUserProductResponses(Integer days, Integer limit);
    List<UserProductResponse> getMostViewedUserProductResponses(Integer days, Integer limit);
    List<UserProductResponse> getBestSellingUserProductResponses(Integer days, Integer limit);
}
