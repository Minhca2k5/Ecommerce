package com.minzetsu.ecommerce.product.service;

import com.minzetsu.ecommerce.product.dto.filter.CategoryFilter;
import com.minzetsu.ecommerce.product.dto.request.CategoryRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminCategoryResponse;
import com.minzetsu.ecommerce.product.dto.response.CategoryResponse;
import com.minzetsu.ecommerce.product.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    void updateCategoryByNameOrSlug(Long id, String name, String slug);
    void deleteCategory(Long id);
    Category getCategoryById(Long id);
    boolean existsById(Long id);
    AdminCategoryResponse createAdminCategoryResponse(CategoryRequest request);
    AdminCategoryResponse getAdminCategoryResponseById(Long id);
    AdminCategoryResponse getFullAdminCategoryResponseById(Long id);
    Page<AdminCategoryResponse> searchAdminCategoryResponses(CategoryFilter filter, Pageable pageable);
    List<AdminCategoryResponse> getAdminSubcategoryResponsesByParentId(Long parentId);

    Page<CategoryResponse> searchCategoryResponses(CategoryFilter filter, Pageable pageable);
    CategoryResponse getCategoryResponseById(Long id);
    CategoryResponse getFullCategoryResponseById(Long id);
    List<CategoryResponse> getSubcategoryResponsesByParentId(Long parentId);
}
