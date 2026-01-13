package com.minzetsu.ecommerce.promotion.service.impl;

import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.order.service.OrderService;
import com.minzetsu.ecommerce.promotion.dto.filter.VoucherUseFilter;
import com.minzetsu.ecommerce.promotion.dto.response.VoucherUseResponse;
import com.minzetsu.ecommerce.promotion.entity.VoucherUse;
import com.minzetsu.ecommerce.promotion.mapper.VoucherUseMapper;
import com.minzetsu.ecommerce.promotion.repository.VoucherUseRepository;
import com.minzetsu.ecommerce.promotion.repository.VoucherUseSpecification;
import com.minzetsu.ecommerce.promotion.service.VoucherService;
import com.minzetsu.ecommerce.promotion.service.VoucherUseService;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class VoucherUserServiceImpl implements VoucherUseService {

    private final VoucherUseRepository voucherUseRepository;
    private final VoucherUseMapper voucherUseMapper;
    private final VoucherService voucherService;
    private final UserService userService;
    private final OrderService orderService;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "voucherPublic", allEntries = true)
    public void createVoucherUse(Long voucherId, Long userId, Long orderId, BigDecimal discountAmount) {
        VoucherUse voucherUse = VoucherUse.builder()
                .voucher(voucherService.getVoucherById(voucherId))
                .order(orderService.getOrderById(orderId))
                .user(userService.getUserById(userId))
                .discountAmount(discountAmount)
                .build();
        voucherUseRepository.save(voucherUse);
    }

    @Override
    @Transactional
    public Page<VoucherUseResponse> getVoucherUseResponsesByUserId(Long userId, Pageable pageable) {
        Page<VoucherUse> voucherUses = voucherUseRepository.findByUserId(userId, pageable);
        return voucherUses.map(voucherUseMapper::toResponse);
    }

    @Override
    @Transactional
    public Page<VoucherUseResponse> getVoucherUseResponseByOrderId(Long orderId, Pageable pageable) {
        Page<VoucherUse> voucherUses = voucherUseRepository.findByOrderId(orderId, pageable);
        return voucherUses.map(voucherUseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherUseResponse> getVoucherUseResponseByVoucherId(Long voucherId, Pageable pageable) {
        Page<VoucherUse> voucherUses = voucherUseRepository.findByVoucherId(voucherId, pageable);
        return voucherUses.map(voucherUseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherUseResponse> searchVoucherUseResponses(VoucherUseFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                voucherUseRepository,
                VoucherUseSpecification.filter(filter),
                voucherUseMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherUseResponse> getVoucherUseResponseByOrderIdAndUserId(Long orderId, Long userId, Pageable pageable) {
        Page<VoucherUse> voucherUses = voucherUseRepository.findByOrderIdAndUserId(orderId, userId, pageable);
        return voucherUses.map(voucherUseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherUseResponse> getVoucherUseResponseByVoucherIdAndUserId(Long voucherId, Long userId, Pageable pageable) {
        Page<VoucherUse> voucherUses = voucherUseRepository.findByVoucherIdAndUserId(voucherId, userId, pageable);
        return voucherUses.map(voucherUseMapper::toResponse);
    }
}
