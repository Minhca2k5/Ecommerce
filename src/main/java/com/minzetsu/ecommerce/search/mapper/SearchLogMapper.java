package com.minzetsu.ecommerce.search.mapper;

import com.minzetsu.ecommerce.search.dto.request.SearchLogRequest;
import com.minzetsu.ecommerce.search.dto.response.SearchLogResponse;
import com.minzetsu.ecommerce.search.entity.SearchLog;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface SearchLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    SearchLog toEntity(SearchLogRequest request);

    @Mapping(target = "userId", source = "user.id")
    SearchLogResponse toResponse(SearchLog log);

    List<SearchLogResponse> toResponseList(List<SearchLog> list);
}
