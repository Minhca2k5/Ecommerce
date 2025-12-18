package com.minzetsu.ecommerce.promotion.service;

import com.minzetsu.ecommerce.promotion.dto.filter.VoucherFilter;
import com.minzetsu.ecommerce.promotion.dto.request.VoucherCreateRequest;
import com.minzetsu.ecommerce.promotion.dto.request.VoucherUpdateRequest;
import com.minzetsu.ecommerce.promotion.dto.response.AdminVoucherResponse;
import com.minzetsu.ecommerce.promotion.dto.response.VoucherResponse;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import com.minzetsu.ecommerce.promotion.entity.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface VoucherService {
    Page<AdminVoucherResponse> searchAdminVoucherResponses(VoucherFilter filter, Pageable pageable);
    AdminVoucherResponse createAdminVoucherResponse(VoucherCreateRequest request);
    AdminVoucherResponse updateAdminVoucherResponse(Long id, VoucherUpdateRequest request);
    void deleteVoucher(Long id);
    Voucher getVoucherById(Long id);
    List<VoucherResponse> getVoucherResponsesByCodeAndActiveStatus(String code, VoucherStatus status);
    Page<VoucherResponse> searchVoucherResponsesByMinOrderTotalAndActiveStatus(BigDecimal minOrderTotal, VoucherStatus status, Pageable pageable);
    VoucherResponse getVoucherResponseById(Long id);
    AdminVoucherResponse getAdminVoucherResponseById(Long id);
}
