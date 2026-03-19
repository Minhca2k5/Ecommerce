package com.minzetsu.ecommerce.review.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.review.dto.request.ReviewRequest;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import com.minzetsu.ecommerce.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Reviews", description = "Quản lý đánh giá sản phẩm dành cho người dùng")
public class UserReviewController {

        private final ReviewService reviewService;

        @Operation(summary = "Xem tất cả đánh giá của tôi", description = "Trả về danh sách toàn bộ các đánh giá mà người dùng hiện tại đã viết.")
        @GetMapping
        public ResponseEntity<List<ReviewResponse>> getMyReviews() {
                Long userId = getCurrentUserId();
                return ResponseEntity.ok(reviewService.getReviewResponseByUserId(userId));
        }

        @Operation(summary = "Tạo đánh giá mới", description = "Người dùng có thể viết một đánh giá mới cho sản phẩm mà họ đã mua.")
        @PostMapping
        public ResponseEntity<ReviewResponse> createMyReview(
                        @Valid @RequestBody ReviewRequest request) {
                Long userId = getCurrentUserId();
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(reviewService.createReviewResponse(request, userId));
        }

        @Operation(summary = "Cập nhật điểm đánh giá", description = "Cập nhật điểm rating cho đánh giá cụ thể thuộc về người dùng hiện tại.")
        @PatchMapping("/{reviewId}/rating")
        public ResponseEntity<Void> updateMyReviewRating(
                        @PathVariable("reviewId") Long reviewId,
                        @RequestParam Integer rating) {
                Long userId = getCurrentUserId();
                reviewService.updateReviewByRating(rating, reviewId, userId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Cập nhật nội dung bình luận", description = "Cập nhật phần bình luận (comment) trong đánh giá của người dùng hiện tại.")
        @PatchMapping("/{reviewId}/comment")
        public ResponseEntity<Void> updateMyReviewComment(
                        @PathVariable("reviewId") Long reviewId,
                        @RequestParam String comment) {
                Long userId = getCurrentUserId();
                reviewService.updateReviewByComment(comment, reviewId, userId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Xóa đánh giá của tôi", description = "Xóa đánh giá cụ thể của người dùng hiện tại theo ID.")
        @DeleteMapping("/{reviewId}")
        public ResponseEntity<Void> deleteMyReview(
                        @PathVariable("reviewId") Long reviewId) {
                Long userId = getCurrentUserId();
                reviewService.deleteReview(reviewId, userId);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/{reviewId}")
        public ResponseEntity<ReviewResponse> getMyReviewById(
                        @PathVariable("reviewId") Long reviewId) {
                return ResponseEntity.ok(reviewService.getReviewResponseByIdAndUserId(reviewId, getCurrentUserId()));
        }

        private Long getCurrentUserId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new com.minzetsu.ecommerce.common.exception.UnAuthorizedException("Unauthenticated");
                }
                return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
}
