package com.minzetsu.ecommerce.promotion.service.impl;

import com.minzetsu.ecommerce.common.audit.entity.AuditAction;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoucherUserServiceImpl implements VoucherUseService {
    private Page<VoucherUseResponse> toDedupedResponsePage(Page<VoucherUse> page, Pageable pageable) {
        List<VoucherUseResponse> mapped = page.stream().map(voucherUseMapper::toResponse).toList();
        Map<String, VoucherUseResponse> unique = new LinkedHashMap<>();
        for (VoucherUseResponse item : mapped) {
            String key;
            Long orderId = item.getOrderId();
            Long voucherId = item.getVoucherId();
            Long userId = item.getUserId();
            if (orderId != null && voucherId != null) {
                key = "order-" + orderId + "-voucher-" + voucherId + "-user-" + userId;
            } else if (item.getId() != null) {
                key = "id-" + item.getId();
            } else {
                key = String.valueOf(item.getCreatedAt()) + "-" + voucherId + "-" + orderId + "-" + userId;
            }
            unique.putIfAbsent(key, item);
        }
        List<VoucherUseResponse> deduped = List.copyOf(unique.values());
        return new PageImpl<>(deduped, pageable, deduped.size());
    }

    private final VoucherUseRepository voucherUseRepository;
    private final VoucherUseMapper voucherUseMapper;
    private final VoucherService voucherService;
    private final UserService userService;
    private final OrderService orderService;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "voucherPublicV2", allEntries = true)
    @AuditAction(action = "VOUCHER_USE_CREATED", entityType = "VOUCHER_USE")
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
        return toDedupedResponsePage(voucherUses, pageable);
    }

    @Override
    @Transactional
    public Page<VoucherUseResponse> getVoucherUseResponseByOrderId(Long orderId, Pageable pageable) {
        Page<VoucherUse> voucherUses = voucherUseRepository.findByOrderId(orderId, pageable);
        return toDedupedResponsePage(voucherUses, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherUseResponse> getVoucherUseResponseByVoucherId(Long voucherId, Pageable pageable) {
        Page<VoucherUse> voucherUses = voucherUseRepository.findByVoucherId(voucherId, pageable);
        return toDedupedResponsePage(voucherUses, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherUseResponse> searchVoucherUseResponses(VoucherUseFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                voucherUseRepository,
                VoucherUseSpecification.filter(filter),
                voucherUseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherUseResponse> getVoucherUseResponseByOrderIdAndUserId(Long orderId, Long userId,
            Pageable pageable) {
        Page<VoucherUse> voucherUses = voucherUseRepository.findByOrderIdAndUserId(orderId, userId, pageable);
        return toDedupedResponsePage(voucherUses, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherUseResponse> getVoucherUseResponseByVoucherIdAndUserId(Long voucherId, Long userId,
            Pageable pageable) {
        Page<VoucherUse> voucherUses = voucherUseRepository.findByVoucherIdAndUserId(voucherId, userId, pageable);
        return toDedupedResponsePage(voucherUses, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrderId(Long orderId) {
        return voucherUseRepository.existsByOrderId(orderId);
    }
}
