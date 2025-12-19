package com.minzetsu.ecommerce.product.controller.pub;

import com.minzetsu.ecommerce.product.dto.filter.CategoryFilter;
import com.minzetsu.ecommerce.product.dto.response.CategoryResponse;
import com.minzetsu.ecommerce.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Xem và tìm kiếm danh mục sản phẩm")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Tìm kiếm danh mục sản phẩm",
            description = "Lọc và tìm kiếm các danh mục theo tên, slug hoặc danh mục cha. Hỗ trợ phân trang."
    )
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> searchCategories(
            @ModelAttribute CategoryFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.searchCategoryResponses(filter, pageable));
    }

    @Operation(
            summary = "Lấy thông tin danh mục theo ID",
            description = "Trả về thông tin cơ bản của danh mục sản phẩm, bao gồm tên và slug."
    )
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getCategoryResponseById(categoryId));
    }

    @Operation(
            summary = "Lấy thông tin danh mục theo slug",
            description = "Trả về thông tin cơ bản của danh mục sản phẩm theo slug, bao gồm tên và slug."
    )
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @PathVariable("slug") String slug
    ) {
        return ResponseEntity.ok(categoryService.getCategoryResponseBySlug(slug));
    }

    @Operation(
            summary = "Lấy chi tiết danh mục",
            description = "Trả về thông tin chi tiết của danh mục, bao gồm danh mục con và các sản phẩm liên quan."
    )
    @GetMapping("/{categoryId}/details")
    public ResponseEntity<CategoryResponse> getFullCategoryById(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getFullCategoryResponseById(categoryId));
    }

    @Operation(
            summary = "Lấy chi tiết danh mục theo slug",
            description = "Trả về thông tin chi tiết của danh mục theo slug, bao gồm danh mục con."
    )
    @GetMapping("/slug/{slug}/details")
    public ResponseEntity<CategoryResponse> getFullCategoryBySlug(
            @PathVariable("slug") String slug
    ) {
        return ResponseEntity.ok(categoryService.getFullCategoryResponseBySlug(slug));
    }

    @Operation(
            summary = "Lấy danh sách danh mục con",
            description = "Trả về danh sách tất cả danh mục con trực tiếp của danh mục cha được chỉ định."
    )
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getSubcategoryResponsesByParentId(categoryId));
    }

    @Operation(
            summary = "Lấy danh sách danh mục con theo slug",
            description = "Trả về danh sách tất cả danh mục con trực tiếp của danh mục cha theo slug."
    )
    @GetMapping("/slug/{slug}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategoriesBySlug(
            @PathVariable("slug") String slug
    ) {
        CategoryResponse parent = categoryService.getCategoryResponseBySlug(slug);
        return ResponseEntity.ok(categoryService.getSubcategoryResponsesByParentId(parent.getId()));
    }
}
