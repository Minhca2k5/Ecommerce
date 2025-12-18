package com.minzetsu.ecommerce.product.controller.user;

import com.minzetsu.ecommerce.product.dto.filter.CategoryFilter;
import com.minzetsu.ecommerce.product.dto.response.UserCategoryResponse;
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
@RequestMapping("/api/users/me/categories")
@RequiredArgsConstructor
@Tag(name = "User - Categories", description = "Xem và tìm kiếm danh mục sản phẩm dành cho người dùng")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Tìm kiếm danh mục sản phẩm",
            description = "Lọc và tìm kiếm các danh mục theo tên, slug hoặc danh mục cha. Hỗ trợ phân trang."
    )
    @GetMapping
    public ResponseEntity<Page<UserCategoryResponse>> searchCategories(
            @ModelAttribute CategoryFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.searchUserCategoryResponses(filter, pageable));
    }

    @Operation(
            summary = "Lấy thông tin danh mục theo ID",
            description = "Trả về thông tin cơ bản của danh mục sản phẩm, bao gồm tên và slug."
    )
    @GetMapping("/{categoryId}")
    public ResponseEntity<UserCategoryResponse> getCategoryById(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getUserCategoryResponseById(categoryId));
    }

    @Operation(
            summary = "Lấy chi tiết danh mục",
            description = "Trả về thông tin chi tiết của danh mục, bao gồm danh mục con và các sản phẩm liên quan."
    )
    @GetMapping("/{categoryId}/details")
    public ResponseEntity<UserCategoryResponse> getFullCategoryById(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getFullUserCategoryResponseById(categoryId));
    }

    @Operation(
            summary = "Lấy danh sách danh mục con",
            description = "Trả về danh sách tất cả danh mục con trực tiếp của danh mục cha được chỉ định."
    )
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<UserCategoryResponse>> getSubcategories(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getUserSubcategoryResponsesByParentId(categoryId));
    }
}
