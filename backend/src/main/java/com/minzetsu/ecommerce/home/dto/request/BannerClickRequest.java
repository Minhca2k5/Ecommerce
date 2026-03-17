package com.minzetsu.ecommerce.home.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BannerClickRequest {

    @NotBlank
    private String bannerKey;

    private String placement;
    private String targetPath;
    private String guestId;
}
