package com.minzetsu.ecommerce.payment.mapper;

import com.minzetsu.ecommerce.payment.dto.request.PaymentRequest;
import com.minzetsu.ecommerce.payment.dto.response.PaymentResponse;
import com.minzetsu.ecommerce.payment.entity.Payment;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface PaymentMapper {

    // request -> entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "order", ignore = true)
    Payment toEntity(PaymentRequest request);

    // entity -> response
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderTotalAmount", source = "order.totalAmount")
    @Mapping(target = "orderCurrency", source = "order.currency")
    @Mapping(target = "orderStatus", source = "order.status")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);

}
