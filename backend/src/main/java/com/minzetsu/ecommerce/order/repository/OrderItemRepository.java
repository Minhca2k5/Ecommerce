package com.minzetsu.ecommerce.order.repository;

import com.minzetsu.ecommerce.order.entity.OrderItem;
import com.minzetsu.ecommerce.product.repository.projection.ProductBestSellingView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, JpaSpecificationExecutor<OrderItem> {
    boolean existsByProductId(Long productId);
    boolean existsByOrderId(Long orderId);
    boolean existsById(Long id);
    List<OrderItem> findByOrderId(Long orderId);
    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);

    @Query(value = "Select sum(oi.quantity) from order_items oi join orders o on oi.order_id = o.id where oi.product_id = :productId and oi.created_at >= NOW() - INTERVAL :days DAY and o.status = 'PAID'", nativeQuery = true)
    Integer getTotalQuantitySoldByProductIdLastDays(Long productId, Integer days);

    @Query(value = "Select oi.product_id as productId, sum(oi.quantity) as totalSold from order_items oi join orders o on oi.order_id = o.id where o.status = 'PAID' and o.created_at >= NOW() - INTERVAL :days DAY group by oi.product_id order by totalSold desc LIMIT :limit", nativeQuery = true)
    List<ProductBestSellingView> getProductBestSellingViewsByTotalQuantitySoldLastDaysAndLimit(Integer days, Integer limit);
}
