package com.minzetsu.ecommerce.user.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    private String username;
    private String email;
    private String fullName;
    private String phone;
}
