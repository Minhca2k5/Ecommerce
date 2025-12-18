package com.minzetsu.ecommerce.search.repository;

import com.minzetsu.ecommerce.search.entity.SearchLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    List<SearchLog> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    Optional<SearchLog> findByUserIdAndKeywordIgnoreCase(Long userId, String keyword);
    void deleteByUserId(Long userId);
    List<SearchLog> findByUserIdAndKeywordContainingIgnoreCase(Long userId, String keyword);
    boolean existsByUserId(Long userId);
}
