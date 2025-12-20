package com.minzetsu.ecommerce.activity.repository;

import com.minzetsu.ecommerce.activity.entity.RecentView;
import com.minzetsu.ecommerce.product.repository.projection.ProductMostViewedView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentViewRepository extends JpaRepository<RecentView, Long> {
    boolean existsByUserId(Long userId);
    boolean existsByProductId(Long productId);
    Page<RecentView> findByUserId(Long userId, Pageable pageable);
    void deleteByUserId(Long userId);
    void deleteByProductId(Long productId);
    Optional<RecentView> findByUserIdAndProductId(Long userId, Long productId);
    @Query("SELECT r FROM RecentView r JOIN FETCH r.product p WHERE p.name LIKE %:productName% and r.user.id = :userId ORDER BY r.updatedAt DESC")
    List<RecentView> findByProductNameOrderByUpdatedAtDesc(String productName, Long userId);
    @Query(value = "Select Count(*) from recent_views where product_id = :productId and created_at >= NOW() - INTERVAL :days DAY", nativeQuery = true)
    Integer countByProductIdLastDays(Long productId, Integer days);

    @Query(value = "Select product_id as productId, Count(*) as totalViews from recent_views where created_at >= NOW() - INTERVAL :days DAY group by product_id order by totalViews desc LIMIT :limit", nativeQuery = true)
    List<ProductMostViewedView> getProductMostViewedViewsByTotalViewedLastDaysAndLimit(Integer days, Integer limit);
}
