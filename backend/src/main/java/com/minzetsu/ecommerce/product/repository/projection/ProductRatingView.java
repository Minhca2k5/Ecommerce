package com.minzetsu.ecommerce.product.repository.projection;

public interface ProductRatingView {
    Long getProductId();
    Double getAverageRating();
    Integer getTotalRatings();
}
