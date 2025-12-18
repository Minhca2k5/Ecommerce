package com.minzetsu.ecommerce.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse extends BaseDTO {

    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Boolean enabled;

    private List<RoleResponse> roles;
    private List<AddressResponse> addresses;
    private List<ReviewResponse> reviews;
    private CartResponse cart;
    private List<OrderResponse> orders;
}
