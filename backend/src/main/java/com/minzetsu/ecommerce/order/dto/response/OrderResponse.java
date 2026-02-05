package com.minzetsu.ecommerce.order.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse extends BaseDTO {

    private Long userId;
    private Long voucherId;
    private String username;
    private String fullName;

    private Long addressIdSnapshot;

    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private BigDecimal discountAmount;
    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private String guestAccessToken;

    // danh sách item
    private List<OrderItemResponse> items;

    // tiện cho FE
    private Integer itemCount;
}
