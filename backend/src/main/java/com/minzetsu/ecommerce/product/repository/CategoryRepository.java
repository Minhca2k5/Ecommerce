package com.minzetsu.ecommerce.product.repository;

import com.minzetsu.ecommerce.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    List<Category> findByParentId(Long parentId);
    Optional<Category> findBySlug(String slug);

    @Modifying
    @Query("""
        UPDATE Category c 
        SET 
            c.name = CASE WHEN :name IS NOT NULL THEN :name ELSE c.name END,
            c.slug = CASE WHEN :slug IS NOT NULL THEN :slug ELSE c.slug END
        WHERE c.id = :id
    """)
    void updateCategoryByNameOrSlug(Long id, String name, String slug);

    boolean existsById(Long id);
}
