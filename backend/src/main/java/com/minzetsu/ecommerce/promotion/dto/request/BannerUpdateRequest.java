package com.minzetsu.ecommerce.promotion.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerUpdateRequest {

    private String title;
    private String imageUrl;
    private String targetUrl;
    private Integer position;
    private Boolean isActive;

    private String startAt;
    private String endAt;
}
