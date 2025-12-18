package com.minzetsu.ecommerce.product.controller.admin;

import com.minzetsu.ecommerce.product.dto.filter.CategoryFilter;
import com.minzetsu.ecommerce.product.dto.request.CategoryRequest;
import com.minzetsu.ecommerce.product.dto.response.AdminCategoryResponse;
import com.minzetsu.ecommerce.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Categories", description = "Quản lý danh mục sản phẩm (category) và phân cấp danh mục")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Tạo danh mục mới",
            description = "Thêm một danh mục mới vào hệ thống. Hỗ trợ danh mục cha nếu có."
    )
    @PostMapping
    public ResponseEntity<AdminCategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createAdminCategoryResponse(request));
    }

    @Operation(
            summary = "Cập nhật tên hoặc slug danh mục",
            description = "Cập nhật tên và/hoặc slug của danh mục dựa trên ID. Có thể truyền một hoặc cả hai giá trị."
    )
    @PatchMapping("/{categoryId}")
    public ResponseEntity<Void> updateCategoryNameOrSlug(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String slug
    ) {
        categoryService.updateCategoryByNameOrSlug(categoryId, name, slug);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Xóa danh mục",
            description = "Xóa một danh mục dựa trên ID. Nếu danh mục có danh mục con, cần xử lý logic phụ thuộc ở service."
    )
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable("categoryId") Long categoryId
    ) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lấy thông tin danh mục theo ID",
            description = "Trả về thông tin cơ bản của danh mục (không bao gồm danh mục con)."
    )
    @GetMapping("/{categoryId}")
    public ResponseEntity<AdminCategoryResponse> getCategoryById(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getAdminCategoryResponseById(categoryId));
    }

    @Operation(
            summary = "Lấy thông tin chi tiết danh mục",
            description = "Trả về thông tin chi tiết của danh mục, bao gồm danh mục con hoặc các sản phẩm liên quan."
    )
    @GetMapping("/{categoryId}/details")
    public ResponseEntity<AdminCategoryResponse> getFullCategoryById(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getFullAdminCategoryResponseById(categoryId));
    }

    @Operation(
            summary = "Tìm kiếm danh mục",
            description = "Lọc và tìm kiếm các danh mục dựa trên tên, slug hoặc danh mục cha. Hỗ trợ phân trang."
    )
    @GetMapping
    public ResponseEntity<Page<AdminCategoryResponse>> searchCategories(
            @ModelAttribute CategoryFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.searchAdminCategoryResponses(filter, pageable));
    }

    @Operation(
            summary = "Lấy danh sách danh mục con",
            description = "Trả về danh sách các danh mục con thuộc một danh mục cha cụ thể."
    )
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<AdminCategoryResponse>> getSubcategories(
            @PathVariable("categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getAdminSubcategoryResponsesByParentId(categoryId));
    }
}
