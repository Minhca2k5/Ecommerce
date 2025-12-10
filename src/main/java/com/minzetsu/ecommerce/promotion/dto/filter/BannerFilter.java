package com.minzetsu.ecommerce.promotion.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerFilter implements SortableFilter {

    private String title;
    private Boolean isActive;
    private Integer position;
    private String startAtFrom;
    private String startAtTo;
    private String endAtFrom;
    private String endAtTo;
    private String sortBy;
    private String sortDirection;
}
