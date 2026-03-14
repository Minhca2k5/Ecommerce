package com.minzetsu.ecommerce.review.service.impl;

import com.minzetsu.ecommerce.common.audit.entity.AuditAction;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.common.exception.InvalidObjectException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.service.ProductService;
import com.minzetsu.ecommerce.review.dto.filter.ReviewFilter;
import com.minzetsu.ecommerce.review.dto.request.ReviewRequest;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import com.minzetsu.ecommerce.review.entity.Review;
import com.minzetsu.ecommerce.review.mapper.ReviewMapper;
import com.minzetsu.ecommerce.review.repository.ReviewRepository;
import com.minzetsu.ecommerce.review.repository.ReviewSpecification;
import com.minzetsu.ecommerce.review.service.ReviewService;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.service.UserService;
import com.minzetsu.ecommerce.notification.event.WebhookEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final ProductService productService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    private Review getExistingReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
    }

    private void validateProductAndUser(Long productId, Long userId) {
        if (!productService.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
    }

    private void validateOwnership(Review review, Long currentUserId) {
        if (!review.getUser().getId().equals(currentUserId)) {
            throw new UnAuthorizedException("You are not authorized to modify this review");
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"productDetail", "home"}, allEntries = true)
    @AuditAction(action = "REVIEW_DELETED", entityType = "REVIEW", idParamIndex = 0)
    public void deleteReview(Long id, Long currentUserId) {
        Review review = getExistingReview(id);
        validateOwnership(review, currentUserId);
        reviewRepository.delete(review);
        eventPublisher.publishEvent(new WebhookEvent(
                "REVIEW_DELETED",
                "REVIEW",
                id,
                currentUserId
        ));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"productDetail", "home"}, allEntries = true)
    @AuditAction(action = "REVIEW_RATING_UPDATED", entityType = "REVIEW", idParamIndex = 1)
    public void updateReviewByRating(Integer rating, Long id, Long currentUserId) {
        Review review = getExistingReview(id);
        validateOwnership(review, currentUserId);
        reviewRepository.updateByRating(rating, id);
        eventPublisher.publishEvent(new WebhookEvent(
                "REVIEW_UPDATED",
                "REVIEW",
                id,
                currentUserId
        ));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"productDetail", "home"}, allEntries = true)
    @AuditAction(action = "REVIEW_COMMENT_UPDATED", entityType = "REVIEW", idParamIndex = 1)
    public void updateReviewByComment(String comment, Long id, Long currentUserId) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new InvalidObjectException("Comment cannot be null or empty");
        }
        Review review = getExistingReview(id);
        validateOwnership(review, currentUserId);
        reviewRepository.updateByComment(comment, id);
        eventPublisher.publishEvent(new WebhookEvent(
                "REVIEW_UPDATED",
                "REVIEW",
                id,
                currentUserId
        ));
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return reviewRepository.existsByProductId(productId);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return reviewRepository.existsByUserId(userId);
    }

    @Override
    public boolean existsById(Long id) {
        return reviewRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewResponseByUserId(Long userId) {
        if (!existsByUserId(userId)) {
            throw new NotFoundException("No reviews found for userId: " + userId);
        }
        List<Review> reviews = reviewRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return reviewMapper.toResponseList(reviews);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"productDetail", "home"}, allEntries = true)
    @AuditAction(action = "REVIEW_CREATED", entityType = "REVIEW")
    public ReviewResponse createReviewResponse(ReviewRequest request, Long userId) {
        request.setUserId(userId);
        Long productId = request.getProductId();

        validateProductAndUser(productId, userId);

        Product product = productService.getProductById(productId);
        User user = userService.getUserById(userId);

        Review review = reviewMapper.toEntity(request);
        review.setProduct(product);
        review.setUser(user);

        Review savedReview = reviewRepository.save(review);
        eventPublisher.publishEvent(new WebhookEvent(
                "REVIEW_CREATED",
                "REVIEW",
                savedReview.getId(),
                userId
        ));
        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewResponseByProductId(Long productId) {
        if (!existsByProductId(productId)) {
            throw new NotFoundException("No reviews found for productId: " + productId);
        }
        List<Review> reviews = reviewRepository.findByProductIdOrderByUpdatedAtDesc(productId);
        return reviewMapper.toResponseList(reviews);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> searchReviewResponses(ReviewFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                reviewRepository,
                ReviewSpecification.filter(filter),
                reviewMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewResponseById(Long id) {
        Review review = getExistingReview(id);
        return reviewMapper.toResponse(review);
    }

    @Override
    public ReviewResponse getReviewResponseByIdAndUserId(Long id, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id + " and userId: " + userId));
        return reviewMapper.toResponse(review);
    }
}

