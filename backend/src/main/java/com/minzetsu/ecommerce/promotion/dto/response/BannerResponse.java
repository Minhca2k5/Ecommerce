package com.minzetsu.ecommerce.promotion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BannerResponse extends BaseDTO {

    private String title;
    private String imageUrl;
    private String targetUrl;

    private Integer position;
    private Boolean isActive;

    private String startAt;
    private String endAt;
}
