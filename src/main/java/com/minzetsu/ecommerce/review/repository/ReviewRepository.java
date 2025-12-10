package com.minzetsu.ecommerce.review.repository;

import com.minzetsu.ecommerce.product.repository.projection.ProductRatingView;
import com.minzetsu.ecommerce.review.entity.Review;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Review r SET r.rating = :rating WHERE r.id = :id")
    void updateByRating(Integer rating, Long id);

    @Modifying
    @Query("UPDATE Review r SET r.comment = :comment WHERE r.id = :id")
    void updateByComment(String comment, Long id);

    void deleteByProductId(Long productId);
    void deleteByUserId(Long userId);

    boolean existsByProductId(Long productId);
    boolean existsByUserId(Long userId);
    boolean existsById(Long id);

    @Query(value = "Select Sum(rating) / Count(*) as averageRating, Sum(rating) as totalRatings from reviews where product_id = :productId and created_at >= NOW() - INTERVAL :days DAY", nativeQuery = true)
    ProductRatingView getProductRatingViewByProductId(Long productId, Integer days);

    @Query(value = "Select product_id as productId, Sum(rating)/Count(*) as averageRating from reviews where created_at >= NOW() - INTERVAL :days DAY group by product_id order by averageRating desc LIMIT :limit", nativeQuery = true)
    List<ProductRatingView> getProductRatingViewsByAverageRatingLastDaysAndLimit(Integer days, Integer limit);
    Optional<Review> findByIdAndUserId(Long id, Long userId);
}
