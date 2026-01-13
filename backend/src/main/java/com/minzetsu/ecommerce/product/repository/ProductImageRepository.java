package com.minzetsu.ecommerce.product.repository;

import com.minzetsu.ecommerce.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);
    Optional<ProductImage> findByIsPrimaryTrueAndProductId(Long productId);
    @Query("SELECT pi FROM ProductImage pi WHERE pi.isPrimary = true AND pi.product.id IN :productIds")
    List<ProductImage> findPrimaryByProductIds(List<Long> productIds);
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.url = :url WHERE pi.id = :id")
    void updateByUrl(String url, Long id);
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isPrimary = :isPrimary WHERE pi.id = :id")
    void updateIsPrimaryById(Boolean isPrimary, Long id);
    boolean existsByProductId(Long productId);
    boolean existsById(Long id);
    boolean existsByIsPrimaryTrueAndProductId(Long productId);
    void deleteByProductId(Long productId);
}
