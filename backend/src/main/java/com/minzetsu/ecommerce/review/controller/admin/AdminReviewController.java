package com.minzetsu.ecommerce.review.controller.admin;

import com.minzetsu.ecommerce.review.dto.filter.ReviewFilter;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import com.minzetsu.ecommerce.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Reviews", description = "Quản lý và tìm kiếm đánh giá sản phẩm")
public class AdminReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Tìm kiếm đánh giá sản phẩm",
            description = "Lọc và tìm kiếm các đánh giá dựa trên điều kiện như ID sản phẩm, người dùng, điểm đánh giá hoặc trạng thái duyệt."
    )
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> searchReviews(
            @ModelAttribute ReviewFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.searchReviewResponses(filter, pageable));
    }

    @GetMapping("/{reviewId}")
    @Operation(
            summary = "Lấy thông tin đánh giá theo ID",
            description = "Trả về thông tin chi tiết của một đánh giá cụ thể theo ID."
    )
    public ResponseEntity<ReviewResponse> getReviewById(
            @PathVariable("reviewId") Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.getReviewResponseById(reviewId));
    }
}
