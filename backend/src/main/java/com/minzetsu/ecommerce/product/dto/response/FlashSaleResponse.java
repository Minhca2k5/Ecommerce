package com.minzetsu.ecommerce.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleResponse {

    private String status;
    private String headline;
    private String description;
    private Instant startAt;
    private Instant endAt;
    private Integer soldToday;
    private Integer soldTarget;
    private Integer leftCount;
    private Integer soldPercent;
    private List<ProductResponse> products;
}
