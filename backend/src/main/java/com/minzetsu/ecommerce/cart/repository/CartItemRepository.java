package com.minzetsu.ecommerce.cart.repository;

import com.minzetsu.ecommerce.cart.entity.CartItem;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByProductId(Long productId);
    boolean existsByCartId(Long cartId);
    boolean existsById(Long id);
    @Modifying
    @Query("""
    UPDATE CartItem c
    SET c.quantity = 
        CASE WHEN :isReturned = true 
             THEN c.quantity - :quantity
             ELSE c.quantity + :quantity 
        END
    WHERE c.id = :id
""")
    void updateQuantityById(
            Integer quantity,
            Long id,
            boolean isReturned
    );

    List<CartItem> findByCartIdOrderByUpdatedAtDesc(Long cartId);
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    Page<CartItem> findByCartId(Long cartId, Pageable pageable);
    @Query("SELECT c FROM CartItem c JOIN FETCH c.product p WHERE p.name LIKE %:productName% and c.cart.user.id = :userId")
    List<CartItem> findByProductName(String productName, Long userId);

    @Query("SELECT c FROM CartItem c JOIN FETCH c.product p WHERE c.product.status = :status AND c.cart.id = :cartId")
    List<CartItem> findByActiveProductTrueAndCartId(Long cartId, ProductStatus status);
}
