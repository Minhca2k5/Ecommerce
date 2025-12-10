package com.minzetsu.ecommerce.user.mapper;

import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.review.dto.response.ReviewResponse;
import com.minzetsu.ecommerce.user.dto.request.UserCreateRequest;
import com.minzetsu.ecommerce.user.dto.request.UserUpdateRequest;
import com.minzetsu.ecommerce.user.dto.response.*;
import com.minzetsu.ecommerce.user.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toEntity(UserCreateRequest request);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);

    default UserResponse toFullResponse(
            User user,
            List<AddressResponse> addresses,
            List<ReviewResponse> reviews,
            CartResponse cart,
            List<OrderResponse> orders,
            @Context RoleMapper roleMapper
    ) {
        UserResponse response = toResponse(user);
        response.setRoles(roleMapper.toResponseList(user.getRoles()));
        response.setAddresses(addresses);
        response.setReviews(reviews);
        response.setCart(cart);
        response.setOrders(orders);
        return response;
    }
}
