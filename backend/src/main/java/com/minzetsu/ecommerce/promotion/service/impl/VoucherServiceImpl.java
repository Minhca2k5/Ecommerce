package com.minzetsu.ecommerce.promotion.service.impl;

import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.promotion.dto.filter.VoucherFilter;
import com.minzetsu.ecommerce.promotion.dto.request.VoucherCreateRequest;
import com.minzetsu.ecommerce.promotion.dto.request.VoucherUpdateRequest;
import com.minzetsu.ecommerce.promotion.dto.response.AdminVoucherResponse;
import com.minzetsu.ecommerce.promotion.dto.response.VoucherResponse;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import com.minzetsu.ecommerce.promotion.entity.VoucherStatus;
import com.minzetsu.ecommerce.promotion.mapper.VoucherMapper;
import com.minzetsu.ecommerce.promotion.repository.VoucherRepository;
import com.minzetsu.ecommerce.promotion.repository.VoucherSpecification;
import com.minzetsu.ecommerce.promotion.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminVoucherResponse> searchAdminVoucherResponses(VoucherFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                voucherRepository,
                VoucherSpecification.filter(filter),
                voucherMapper::toAdminResponse
        );
    }

    @Override
    @Transactional
    public AdminVoucherResponse createAdminVoucherResponse(VoucherCreateRequest request) {
        Voucher voucher = voucherMapper.toEntity(request);
        voucher = voucherRepository.save(voucher);
        return voucherMapper.toAdminResponse(voucher);
    }

    @Override
    @Transactional
    public AdminVoucherResponse updateAdminVoucherResponse(Long id, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found"));
        voucherMapper.updateEntity(voucher, request);
        voucher = voucherRepository.save(voucher);
        return voucherMapper.toAdminResponse(voucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found"));
        voucherRepository.delete(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public Voucher getVoucherById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getVoucherResponsesByCodeAndActiveStatus(String code, VoucherStatus status) {
        return voucherMapper.toUserResponseList(voucherRepository.findByCodeContainingIgnoreCaseAndStatus(code, status));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> searchVoucherResponsesByMinOrderTotalAndActiveStatus(BigDecimal minOrderTotal, VoucherStatus status, Pageable pageable) {
        Page<Voucher> vouchers = voucherRepository.findByMinOrderTotalLessThanEqualAndStatus(minOrderTotal, status, pageable);
        return vouchers.map(voucherMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getVoucherResponseById(Long id) {
        Voucher voucher = getVoucherById(id);
        return voucherMapper.toUserResponse(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminVoucherResponse getAdminVoucherResponseById(Long id) {
        Voucher voucher = getVoucherById(id);
        return voucherMapper.toAdminResponse(voucher);
    }
}
