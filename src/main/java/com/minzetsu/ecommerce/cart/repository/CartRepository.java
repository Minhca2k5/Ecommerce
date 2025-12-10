package com.minzetsu.ecommerce.cart.repository;

import com.minzetsu.ecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    boolean existsByUserId(Long userId);
    boolean existsById(Long id);
    Optional<Cart> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
