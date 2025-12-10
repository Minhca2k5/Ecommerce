package com.minzetsu.ecommerce.promotion.mapper;

import com.minzetsu.ecommerce.promotion.dto.response.VoucherUseResponse;
import com.minzetsu.ecommerce.promotion.entity.VoucherUse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface VoucherUseMapper {

    @Mapping(target = "voucherId", source = "voucher.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderId", source = "order.id")
    VoucherUseResponse toResponse(VoucherUse use);

    List<VoucherUseResponse> toResponseList(List<VoucherUse> list);
}
