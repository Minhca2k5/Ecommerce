package com.minzetsu.ecommerce.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String message;

    // sau này bạn sẽ thêm các trường token vào đây khi học JWT:
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}
