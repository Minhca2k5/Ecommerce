package com.minzetsu.ecommerce.user.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressUpdateRequest {

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String country;
    private String zipcode;
}
