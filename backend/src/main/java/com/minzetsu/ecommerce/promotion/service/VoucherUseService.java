package com.minzetsu.ecommerce.promotion.service;

import com.minzetsu.ecommerce.promotion.dto.filter.VoucherUseFilter;
import com.minzetsu.ecommerce.promotion.dto.response.VoucherUseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface VoucherUseService {
    void createVoucherUse(Long voucherId, Long userId, Long orderId, BigDecimal discountAmount);
    Page<VoucherUseResponse> getVoucherUseResponsesByUserId(Long userId, Pageable pageable);
    Page<VoucherUseResponse> getVoucherUseResponseByOrderId(Long orderId, Pageable pageable);
    Page<VoucherUseResponse> getVoucherUseResponseByVoucherId(Long voucherId, Pageable pageable);
    Page<VoucherUseResponse> searchVoucherUseResponses(VoucherUseFilter filter, Pageable pageable);
    Page<VoucherUseResponse> getVoucherUseResponseByOrderIdAndUserId(Long orderId, Long userId, Pageable pageable);
    Page<VoucherUseResponse> getVoucherUseResponseByVoucherIdAndUserId(Long voucherId, Long userId, Pageable pageable);
    boolean existsByOrderId(Long orderId);
}
