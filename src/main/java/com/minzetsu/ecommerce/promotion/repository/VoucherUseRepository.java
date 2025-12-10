package com.minzetsu.ecommerce.promotion.repository;

import com.minzetsu.ecommerce.promotion.entity.VoucherUse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherUseRepository extends JpaRepository<VoucherUse, Long>, JpaSpecificationExecutor<VoucherUse> {
    Optional<VoucherUse> findByVoucherId(Long voucherId);
    Page<VoucherUse> findByUserId(Long userId, Pageable pageable);
    Page<VoucherUse> findByOrderId(Long orderId, Pageable pageable);
    Page<VoucherUse> findByVoucherId(Long voucherId, Pageable pageable);
    Page<VoucherUse> findByOrderIdAndUserId(Long orderId, Long userId, Pageable pageable);
    Page<VoucherUse> findByVoucherIdAndUserId(Long voucherId, Long userId, Pageable pageable);
}
