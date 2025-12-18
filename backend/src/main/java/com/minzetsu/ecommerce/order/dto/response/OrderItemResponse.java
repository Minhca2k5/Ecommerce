package com.minzetsu.ecommerce.order.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemResponse extends BaseDTO {

    private Long orderId;

    private Long productId;

    // snapshot từ order_items
    private String productNameSnapshot;
    private BigDecimal unitPriceSnapshot;
    private Integer quantity;
    private BigDecimal lineTotal;

    // thông tin hiện tại của product (nếu còn tồn tại) – bày vẽ hợp lý
    private String productSlug;
    private String productSku;
    private String url;
}
