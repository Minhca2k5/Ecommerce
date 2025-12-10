package com.minzetsu.ecommerce.promotion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String imageUrl;

    private String targetUrl;

    private Integer position;

    private Boolean isActive;

    private String startAt;
    private String endAt;
}
