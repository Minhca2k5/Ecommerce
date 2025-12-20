package com.minzetsu.ecommerce.activity.mapper;

import com.minzetsu.ecommerce.activity.dto.request.RecentViewRequest;
import com.minzetsu.ecommerce.activity.dto.response.RecentViewResponse;
import com.minzetsu.ecommerce.activity.entity.RecentView;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface RecentViewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    RecentView toEntity(RecentViewRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "productCurrency", source = "product.currency")
    @Mapping(target = "productStatus", source = "product.status")
    @Mapping(target = "url", ignore = true)
    RecentViewResponse toResponse(RecentView view);

    List<RecentViewResponse> toResponseList(List<RecentView> list);
}
