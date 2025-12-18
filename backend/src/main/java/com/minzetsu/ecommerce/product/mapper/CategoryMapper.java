package com.minzetsu.ecommerce.product.mapper;

import com.minzetsu.ecommerce.product.dto.request.CategoryRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminCategoryResponse;
import com.minzetsu.ecommerce.product.dto.response.AdminProductResponse;
import com.minzetsu.ecommerce.product.dto.response.CategoryResponse;
import com.minzetsu.ecommerce.product.entity.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryRequest request);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    AdminCategoryResponse toAdminResponse(Category category);

    List<AdminCategoryResponse> toAdminResponseList(List<Category> categories);

    default AdminCategoryResponse toFullAdminResponse(
            Category category,
            List<AdminProductResponse> products,
            List<AdminCategoryResponse> subcategories
    ) {
        AdminCategoryResponse response = toAdminResponse(category);
        response.setProducts(products);
        response.setSubcategories(subcategories);
        return response;
    }

    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    default CategoryResponse toFullResponse(
            Category category,
            List<CategoryResponse> subcategories
    ) {
        CategoryResponse response = toResponse(category);
        response.setSubcategories(subcategories);
        return response;
    }
}
