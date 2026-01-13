package com.minzetsu.ecommerce.product.service.impl;

import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.product.dto.filter.CategoryFilter;
import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.dto.request.CategoryRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminCategoryResponse;
import com.minzetsu.ecommerce.product.dto.response.AdminProductResponse;
import com.minzetsu.ecommerce.product.dto.response.CategoryResponse;
import com.minzetsu.ecommerce.product.entity.Category;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.mapper.CategoryMapper;
import com.minzetsu.ecommerce.product.mapper.ProductMapper;
import com.minzetsu.ecommerce.product.repository.CategoryRepository;
import com.minzetsu.ecommerce.product.repository.CategorySpecification;
import com.minzetsu.ecommerce.product.repository.ProductRepository;
import com.minzetsu.ecommerce.product.repository.ProductSpecification;
import com.minzetsu.ecommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    private Category getExistingCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    private Category getExistingCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Category not found with slug: " + slug));
    }

    private void unlinkRelationsBeforeDelete(Long categoryId) {
        // Gỡ liên kết category khỏi subcategories
        List<Category> subcategories = categoryRepository.findByParentId(categoryId);
        for (Category subcategory : subcategories) {
            subcategory.setParent(null);
            categoryRepository.save(subcategory);
        }

        // Gỡ liên kết category khỏi products
        ProductFilter filter = ProductFilter.builder().categoryId(categoryId).build();
        List<Product> products = productRepository.findAll(ProductSpecification.filter(filter));
        for (Product product : products) {
            product.setCategory(null);
            productRepository.save(product);
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"home", "categoryDetail", "categoryTree"}, allEntries = true)
    public void updateCategoryByNameOrSlug(Long id, String name, String slug) {
        if (!existsById(id)) {
            throw new NotFoundException("Category not found with id: " + id);
        }
        categoryRepository.updateCategoryByNameOrSlug(id, name, slug);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"home", "categoryDetail", "categoryTree"}, allEntries = true)
    public void deleteCategory(Long id) {
        Category category = getExistingCategory(id);
        unlinkRelationsBeforeDelete(id);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return getExistingCategory(id);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"home", "categoryDetail", "categoryTree"}, allEntries = true)
    public AdminCategoryResponse createAdminCategoryResponse(CategoryRequest request) {
        Long parentId = request.getParentId();
        if (parentId != null && !existsById(parentId)) {
            throw new NotFoundException("Parent category not found with id: " + parentId);
        }

        Category category = categoryMapper.toEntity(request);
        if (parentId != null) {
            category.setParent(getExistingCategory(parentId));
        }
        return categoryMapper.toAdminResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCategoryResponse getAdminCategoryResponseById(Long id) {
        return categoryMapper.toAdminResponse(getExistingCategory(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCategoryResponse getFullAdminCategoryResponseById(Long id) {
        Category category = getExistingCategory(id);
        List<AdminCategoryResponse> subCategoryResponses = getAdminSubcategoryResponsesByParentId(id);

        ProductFilter filter = ProductFilter.builder()
                .categoryId(id)
                .build();

        List<AdminProductResponse> productResponses =
                productMapper.toAdminResponseList(productRepository.findAll(ProductSpecification.filter(filter)));

        return categoryMapper.toFullAdminResponse(category, productResponses, subCategoryResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCategoryResponse> searchAdminCategoryResponses(CategoryFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                categoryRepository,
                CategorySpecification.filter(filter),
                categoryMapper::toAdminResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> getAdminSubcategoryResponsesByParentId(Long parentId) {
        return categoryMapper.toAdminResponseList(categoryRepository.findByParentId(parentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategoryResponses(CategoryFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                categoryRepository,
                CategorySpecification.filter(filter),
                categoryMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryResponseById(Long id) {
        return categoryMapper.toResponse(getExistingCategory(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categoryDetail", key = "'v1:' + #id")
    public CategoryResponse getFullCategoryResponseById(Long id) {
        Category category = getExistingCategory(id);
        List<CategoryResponse> subCategoryResponses = getSubcategoryResponsesByParentId(id);
        return categoryMapper.toFullResponse(category, subCategoryResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryResponseBySlug(String slug) {
        return categoryMapper.toResponse(getExistingCategoryBySlug(slug));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categoryDetail", key = "'v1:slug:' + #slug")
    public CategoryResponse getFullCategoryResponseBySlug(String slug) {
        Category category = getExistingCategoryBySlug(slug);
        List<CategoryResponse> subCategoryResponses = getSubcategoryResponsesByParentId(category.getId());
        return categoryMapper.toFullResponse(category, subCategoryResponses);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categoryTree", key = "'v1:parent:' + #parentId")
    public List<CategoryResponse> getSubcategoryResponsesByParentId(Long parentId) {
        return categoryMapper.toResponseList(categoryRepository.findByParentId(parentId));
    }
}
