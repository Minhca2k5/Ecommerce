package com.minzetsu.ecommerce.promotion.repository;

import com.minzetsu.ecommerce.promotion.entity.Voucher;
import com.minzetsu.ecommerce.promotion.entity.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {
    List<Voucher> findByCodeContainingIgnoreCaseAndStatus(String code, VoucherStatus status);
    Page<Voucher> findByMinOrderTotalLessThanEqualAndStatus(BigDecimal minOrderTotal, VoucherStatus status, Pageable pageable);

}
