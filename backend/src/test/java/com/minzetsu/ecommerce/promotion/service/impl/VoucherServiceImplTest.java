package com.minzetsu.ecommerce.promotion.service.impl;

import com.minzetsu.ecommerce.common.exception.DeletionException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.messaging.DomainEventPublisher;
import com.minzetsu.ecommerce.notification.event.WebhookEvent;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import com.minzetsu.ecommerce.promotion.dto.response.UserVoucherResponse;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import com.minzetsu.ecommerce.promotion.entity.VoucherStatus;
import com.minzetsu.ecommerce.promotion.mapper.VoucherMapper;
import com.minzetsu.ecommerce.promotion.repository.VoucherRepository;
import com.minzetsu.ecommerce.promotion.repository.VoucherUseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private VoucherMapper voucherMapper;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private VoucherUseRepository voucherUseRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private DomainEventPublisher domainEventPublisher;

    private VoucherServiceImpl voucherService;

    @BeforeEach
    void setUp() {
        voucherService = new VoucherServiceImpl(
                voucherRepository,
                voucherMapper,
                orderRepository,
                voucherUseRepository,
                eventPublisher,
                domainEventPublisher
        );
    }

    @Test
    void deleteVoucher_shouldThrowWhenVoucherLinkedToOrders() {
        Voucher voucher = Voucher.builder().code("SALE").name("Sale").build();
        voucher.setId(10L);

        when(voucherRepository.findById(10L)).thenReturn(Optional.of(voucher));
        when(orderRepository.existsByVoucherId(10L)).thenReturn(true);

        assertThatThrownBy(() -> voucherService.deleteVoucher(10L))
                .isInstanceOf(DeletionException.class)
                .hasMessageContaining("associated with existing orders");

        verify(voucherRepository, never()).delete(any(Voucher.class));
    }

    @Test
    void getVoucherResponseById_shouldReturnResponseWhenVoucherIsValidForUser() {
        Voucher voucher = Voucher.builder()
                .code("SAVE10")
                .name("Save 10")
                .status(VoucherStatus.ACTIVE)
                .usageLimitGlobal(5)
                .usageLimitUser(2)
                .build();
        voucher.setId(20L);

        UserVoucherResponse mapped = UserVoucherResponse.builder()
                .code("SAVE10")
                .name("Save 10")
                .build();

        when(voucherRepository.findById(20L)).thenReturn(Optional.of(voucher));
        when(voucherUseRepository.countByVoucherId(20L)).thenReturn(1);
        when(voucherUseRepository.countByVoucherIdAndUserId(20L, 99L)).thenReturn(1);
        when(voucherMapper.toUserResponse(voucher)).thenReturn(mapped);

        UserVoucherResponse result = voucherService.getVoucherResponseById(20L, 99L);

        assertThat(result.getCode()).isEqualTo("SAVE10");
        assertThat(result.getActiveUsesForUser()).isEqualTo(1);
    }

    @Test
    void getVoucherResponseById_shouldThrowNotFoundWhenUserReachedUsageLimit() {
        Voucher voucher = Voucher.builder()
                .code("SAVE10")
                .name("Save 10")
                .status(VoucherStatus.ACTIVE)
                .usageLimitGlobal(5)
                .usageLimitUser(2)
                .build();
        voucher.setId(21L);

        when(voucherRepository.findById(21L)).thenReturn(Optional.of(voucher));
        when(voucherUseRepository.countByVoucherId(21L)).thenReturn(1);
        when(voucherUseRepository.countByVoucherIdAndUserId(21L, 100L)).thenReturn(2);

        assertThatThrownBy(() -> voucherService.getVoucherResponseById(21L, 100L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Voucher not found");
    }

    @Test
    void deleteVoucher_shouldDeleteAndPublishEventsWhenNotLinked() {
        Voucher voucher = Voucher.builder().code("SALE2").name("Sale 2").build();
        voucher.setId(30L);

        when(voucherRepository.findById(30L)).thenReturn(Optional.of(voucher));
        when(orderRepository.existsByVoucherId(30L)).thenReturn(false);

        voucherService.deleteVoucher(30L);

        verify(voucherRepository).delete(voucher);
        verify(eventPublisher).publishEvent(any(WebhookEvent.class));
        verify(domainEventPublisher).publish(eq(com.minzetsu.ecommerce.messaging.DomainEventType.VOUCHER_DELETED), eq(30L), eq(null), any());
    }
}
