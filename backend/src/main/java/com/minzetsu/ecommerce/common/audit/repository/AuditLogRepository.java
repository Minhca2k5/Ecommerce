package com.minzetsu.ecommerce.common.audit.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import com.minzetsu.ecommerce.common.audit.entity.AuditLog;


public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    @Query("select a.id from AuditLog a where a.createdAt < :cutoff order by a.createdAt asc")
    List<Long> findIdsForRetentionCleanup(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);
}




