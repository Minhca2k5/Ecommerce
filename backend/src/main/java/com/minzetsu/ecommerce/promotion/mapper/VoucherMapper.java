package com.minzetsu.ecommerce.promotion.mapper;

import com.minzetsu.ecommerce.promotion.dto.request.VoucherCreateRequest;
import com.minzetsu.ecommerce.promotion.dto.request.VoucherUpdateRequest;
import com.minzetsu.ecommerce.promotion.dto.response.AdminVoucherResponse;
import com.minzetsu.ecommerce.promotion.dto.response.UserVoucherResponse;
import com.minzetsu.ecommerce.promotion.entity.Voucher;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface VoucherMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Voucher toEntity(VoucherCreateRequest request);

    void updateEntity(@MappingTarget Voucher voucher, VoucherUpdateRequest request);

    @Mapping(target = "activeUses", ignore = true)
    AdminVoucherResponse toAdminResponse(Voucher voucher);

    List<AdminVoucherResponse> toAdminResponseList(List<Voucher> list);

    @Mapping(target = "activeUsesForUser", ignore = true)
    UserVoucherResponse toUserResponse(Voucher voucher);
    List<UserVoucherResponse> toUserResponseList(List<Voucher> list);
}
