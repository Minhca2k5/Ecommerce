package com.minzetsu.ecommerce.promotion.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherUseFilter implements SortableFilter {

    private Long voucherId;

    private Long userId;

    private Long orderId;

    // range for discount amount
    private Double discountAmountFrom;
    private Double discountAmountTo;

    // date range
    private String createdAtFrom;
    private String createdAtTo;

    private String sortBy;
    private String sortDirection;
}
