package com.minzetsu.ecommerce.order.repository;

import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    boolean existsByUserId(Long userId);
    boolean existsByVoucherId(Long voucherId);
    boolean existsById(Long id);

    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    void updateStatusById(Long id, OrderStatus status);

    @Modifying
    @Query("UPDATE Order o SET o.currency = :currency WHERE o.id = :id")
    void updateCurrencyById(Long id, String currency);

    List<Order> findByUserId(Long userId);
}
