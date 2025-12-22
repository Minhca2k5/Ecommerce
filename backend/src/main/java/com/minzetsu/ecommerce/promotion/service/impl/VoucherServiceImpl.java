package com.minzetsu.ecommerce.promotion.service.impl;

import com.minzetsu.ecommerce.common.exception.DeletionException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import com.minzetsu.ecommerce.promotion.dto.filter.VoucherFilter;
import com.minzetsu.ecommerce.promotion.dto.request.VoucherCreateRequest;
import com.minzetsu.ecommerce.promotion.dto.request.VoucherUpdateRequest;
import com.minzetsu.ecommerce.promotion.dto.response.AdminVoucherResponse;
import com.minzetsu.ecommerce.promotion.dto.response.UserVoucherResponse;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import com.minzetsu.ecommerce.promotion.entity.VoucherStatus;
import com.minzetsu.ecommerce.promotion.mapper.VoucherMapper;
import com.minzetsu.ecommerce.promotion.repository.VoucherRepository;
import com.minzetsu.ecommerce.promotion.repository.VoucherSpecification;
import com.minzetsu.ecommerce.promotion.repository.VoucherUseRepository;
import com.minzetsu.ecommerce.promotion.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final OrderRepository orderRepository;
    private final VoucherUseRepository voucherUseRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminVoucherResponse> searchAdminVoucherResponses(VoucherFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                voucherRepository,
                VoucherSpecification.filter(filter),
                this::toAdminResponse
        );
    }

    @Override
    @Transactional
    public AdminVoucherResponse createAdminVoucherResponse(VoucherCreateRequest request) {
        Voucher voucher = voucherMapper.toEntity(request);
        voucher = voucherRepository.save(voucher);
        return toAdminResponse(voucher);
    }

    @Override
    @Transactional
    public AdminVoucherResponse updateAdminVoucherResponse(Long id, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found"));
        voucherMapper.updateEntity(voucher, request);
        voucher = voucherRepository.save(voucher);
        return toAdminResponse(voucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found"));
        if (orderRepository.existsByVoucherId(id)) {
            throw new DeletionException("Cannot delete voucher associated with existing orders");
        }
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
    public List<UserVoucherResponse> getVoucherResponsesByCode(String code, Long userId) {
        List<Voucher> vouchers = voucherRepository.findByCodeContainingIgnoreCase(code);
        vouchers.removeIf(voucher -> !isValidVoucherForUser(voucher, userId));
        return toUserResponseList(vouchers, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserVoucherResponse> searchVoucherResponsesByMinOrderTotal(BigDecimal minOrderTotal, Long userId, Pageable pageable) {
        Page<Voucher> vouchers = voucherRepository.findByMinOrderTotalLessThanEqual(minOrderTotal, pageable);
        List<Voucher> filteredVouchers = vouchers.getContent().stream()
                .filter(voucher -> isValidVoucherForUser(voucher, userId))
                .toList();
        vouchers = new PageImpl<>(filteredVouchers, vouchers.getPageable(), vouchers.getTotalElements());
        return vouchers.map(voucher -> toUserResponse(voucher, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserVoucherResponse getVoucherResponseById(Long id, Long userId) {
        Voucher voucher = getVoucherById(id);
        if (!isValidVoucherForUser(voucher, userId)) {
            throw new NotFoundException("Voucher not found");
        }
        return toUserResponse(voucher, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminVoucherResponse getAdminVoucherResponseById(Long id) {
        Voucher voucher = getVoucherById(id);
        return toAdminResponse(voucher);
    }

    private boolean isValidVoucherForSystem(Voucher voucher) {
        Integer usageCount = voucherUseRepository.countByVoucherId(voucher.getId());
        return voucher.getStatus() != VoucherStatus.ACTIVE || (usageCount == null || usageCount >= voucher.getUsageLimitGlobal());
    }

    private boolean isValidVoucherForUser(Voucher voucher, Long userId) {
        if (isValidVoucherForSystem(voucher)) {
            return false;
        }
        Integer userUsageCount = voucherUseRepository.countByVoucherIdAndUserId(voucher.getId(), userId);
        return userUsageCount != null && userUsageCount < voucher.getUsageLimitUser();
    }

    private AdminVoucherResponse toAdminResponse(Voucher voucher) {
        AdminVoucherResponse response = voucherMapper.toAdminResponse(voucher);
        Integer usageCount = voucherUseRepository.countByVoucherId(voucher.getId());
        Integer usageLimitGlobal = voucher.getUsageLimitGlobal();
        response.setActiveUses(usageCount != null ? usageLimitGlobal - usageCount : usageLimitGlobal);
        return response;
    }

    private List<AdminVoucherResponse> toAdminResponseList(List<Voucher> vouchers) {
        return vouchers.stream()
                .map(this::toAdminResponse)
                .toList();
    }

    private UserVoucherResponse toUserResponse(Voucher voucher, Long userId) {
        if (isValidVoucherForSystem(voucher)) {
            throw new NotFoundException("Voucher not found");
        }
        UserVoucherResponse response = voucherMapper.toUserResponse(voucher);
        Integer userUsageCount = voucherUseRepository.countByVoucherIdAndUserId(voucher.getId(), userId);
        Integer usageLimitUser = voucher.getUsageLimitUser();
        response.setActiveUsesForUser(userUsageCount != null ? usageLimitUser - userUsageCount : usageLimitUser);
        return response;
    }

    private List<UserVoucherResponse> toUserResponseList(List<Voucher> vouchers, Long userId) {
        return vouchers.stream()
                .map(voucher -> toUserResponse(voucher, userId))
                .toList();
    }
}
