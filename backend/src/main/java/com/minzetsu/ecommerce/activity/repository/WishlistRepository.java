package com.minzetsu.ecommerce.activity.repository;

import com.minzetsu.ecommerce.activity.entity.Wishlist;
import com.minzetsu.ecommerce.product.repository.projection.ProductMostFavoriteView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    @EntityGraph(attributePaths = {"product"})
    Page<Wishlist> findByUserId(Long userId, Pageable pageable);
    long countByUserId(Long userId);
    long countByProductId(Long productId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    boolean existsByProductId(Long productId);
    boolean existsByUserId(Long userId);
    void deleteByUserId(Long userId);
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product p WHERE p.name LIKE %:productName% and w.user.id = :userId ORDER BY w.updatedAt DESC")
    List<Wishlist> findByProductNameByOrderByUpdatedAtDesc(String productName, Long userId);
    @Query(value = "Select Count(*) from wishlists where product_id = :productId and created_at >= NOW() - INTERVAL :days DAY", nativeQuery = true)
    Integer countByProductIdLastDays(Long productId, Integer days);

    @Query(value = "Select product_id as productId, Count(*) as totalFavorites from wishlists where created_at >= NOW() - INTERVAL :days DAY group by product_id order by totalFavorites desc LIMIT :limit", nativeQuery = true)
    List<ProductMostFavoriteView> getProductMostFavoriteViewsByTotalFavoriteLastDaysAndLimit(Integer days, Integer limit);
}
