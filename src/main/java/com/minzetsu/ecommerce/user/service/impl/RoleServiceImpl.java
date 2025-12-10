package com.minzetsu.ecommerce.user.service.impl;

import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.user.dto.request.RoleRequest;
import com.minzetsu.ecommerce.user.dto.response.RoleResponse;
import com.minzetsu.ecommerce.user.entity.Role;
import com.minzetsu.ecommerce.user.mapper.RoleMapper;
import com.minzetsu.ecommerce.user.repository.RoleRepository;
import com.minzetsu.ecommerce.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    private Role getExistingRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found with id: " + id));
    }

    private Role getExistingRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Role not found with name: " + name));
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return getExistingRoleByName(name);
    }

    @Override
    @Transactional
    public void deleteRoleById(Long id) {
        Role role = getExistingRole(id);
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleResponse createRoleResponse(RoleRequest request) {
        Role role = roleMapper.toEntity(request);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toResponse(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoleResponses() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toResponseList(roles);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleResponseById(Long id) {
        Role role = getExistingRole(id);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleResponseByName(String name) {
        Role role = getExistingRoleByName(name);
        return roleMapper.toResponse(role);
    }
}
