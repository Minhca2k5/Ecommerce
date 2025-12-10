package com.minzetsu.ecommerce.user.mapper;

import com.minzetsu.ecommerce.user.dto.request.AddressCreateRequest;
import com.minzetsu.ecommerce.user.dto.request.AddressUpdateRequest;
import com.minzetsu.ecommerce.user.dto.response.AddressResponse;
import com.minzetsu.ecommerce.user.entity.Address;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Address toEntity(AddressCreateRequest request);

    @Mapping(target = "userId", source = "user.id")
    AddressResponse toResponse(Address entity);

    List<AddressResponse> toResponseList(List<Address> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateAddressFromRequest(AddressUpdateRequest request, @MappingTarget Address address);

}
