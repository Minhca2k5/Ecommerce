package com.minzetsu.ecommerce.user.mapper;

import com.minzetsu.ecommerce.user.dto.request.RoleRequest;
import com.minzetsu.ecommerce.user.dto.response.RoleResponse;
import com.minzetsu.ecommerce.user.entity.Role;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role entity);

    List<RoleResponse> toResponseList(List<Role> entities);
}
