package com.minzetsu.ecommerce.user.service;

import com.minzetsu.ecommerce.user.dto.request.RoleRequest;
import com.minzetsu.ecommerce.user.dto.response.RoleResponse;
import com.minzetsu.ecommerce.user.entity.Role;

import java.util.List;

public interface RoleService {
    Role getRoleByName(String name);
    void deleteRoleById(Long id);

    RoleResponse createRoleResponse(RoleRequest request);
    List<RoleResponse> getAllRoleResponses();
    RoleResponse getRoleResponseById(Long id);
    RoleResponse getRoleResponseByName(String name);
}
