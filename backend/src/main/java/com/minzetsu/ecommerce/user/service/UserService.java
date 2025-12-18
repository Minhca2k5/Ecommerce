package com.minzetsu.ecommerce.user.service;

import com.minzetsu.ecommerce.user.dto.filter.UserFilter;
import com.minzetsu.ecommerce.user.dto.request.PasswordRequest;
import com.minzetsu.ecommerce.user.dto.request.UserCreateRequest;
import com.minzetsu.ecommerce.user.dto.request.UserUpdateRequest;
import com.minzetsu.ecommerce.user.dto.response.UserResponse;
import com.minzetsu.ecommerce.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteUser(Long userId);
    User getUserById(Long id);
    boolean existsById(Long id);


    UserResponse getUserResponseById(Long id);
    UserResponse getFullUserResponseById(Long id);
    UserResponse updateUserResponse(UserUpdateRequest request, Long id);
    UserResponse changeUserPassword(Long id, PasswordRequest request);
    UserResponse createUserResponse(UserCreateRequest request);
    Page<UserResponse> searchUserResponses(UserFilter filter, Pageable pageable);
}
