package com.minzetsu.ecommerce.promotion.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherFilter implements SortableFilter {
    private String name;
    private String code;
    private String discountType;
    private String status;

    private Double discountValueFrom;
    private Double discountValueTo;

    private Double minOrderTotalFrom;
    private Double minOrderTotalTo;

    private String startAtFrom;
    private String startAtTo;

    private String endAtFrom;
    private String endAtTo;

    private Integer usageLimitGlobal;
    private Integer usageLimitUser;

    private String sortBy;
    private String sortDirection;
}
