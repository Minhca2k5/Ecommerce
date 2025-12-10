package com.minzetsu.ecommerce.review.service;

import com.minzetsu.ecommerce.review.dto.filter.ReviewFilter;
import com.minzetsu.ecommerce.review.dto.request.ReviewRequest;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    void deleteReview(Long id, Long currentUserId);
    void updateReviewByRating(Integer rating, Long id, Long currentUserId);
    void updateReviewByComment(String comment, Long id, Long currentUserId);
    boolean existsByProductId(Long productId);
    boolean existsByUserId(Long userId);
    boolean existsById(Long id);

    List<ReviewResponse> getReviewResponseByUserId(Long userId);
    ReviewResponse createReviewResponse(ReviewRequest request, Long userId);
    List<ReviewResponse> getReviewResponseByProductId(Long productId);
    Page<ReviewResponse> searchReviewResponses(ReviewFilter filter, Pageable pageable);
    ReviewResponse getReviewResponseById(Long id);
    ReviewResponse getReviewResponseByIdAndUserId(Long id, Long userId);
}
