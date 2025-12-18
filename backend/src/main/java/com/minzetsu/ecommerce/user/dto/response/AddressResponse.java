package com.minzetsu.ecommerce.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressResponse extends BaseDTO {

    private Long userId;

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private Boolean isDefault;
}
