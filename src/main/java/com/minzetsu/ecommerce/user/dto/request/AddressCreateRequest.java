package com.minzetsu.ecommerce.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressCreateRequest {
    private Long userId;
    @NotBlank(message = "Address line 1 is required")
    private String line1;
    private String line2;
    @NotBlank(message = "City is required")
    private String city;
    private String state;
    @NotBlank(message = "Country is required")
    private String country;
    private String zipcode;

    @Builder.Default
    private Boolean isDefault = false;
}
