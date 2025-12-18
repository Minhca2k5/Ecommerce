package com.minzetsu.ecommerce.promotion.mapper;

import com.minzetsu.ecommerce.promotion.dto.request.BannerCreateRequest;
import com.minzetsu.ecommerce.promotion.dto.request.BannerUpdateRequest;
import com.minzetsu.ecommerce.promotion.dto.response.BannerResponse;
import com.minzetsu.ecommerce.promotion.entity.Banner;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface BannerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Banner toEntity(BannerCreateRequest request);

    void updateEntity(@MappingTarget Banner banner, BannerUpdateRequest request);

    BannerResponse toResponse(Banner banner);

    List<BannerResponse> toResponseList(List<Banner> list);
}
