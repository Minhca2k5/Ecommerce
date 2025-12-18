package com.minzetsu.ecommerce.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse extends BaseDTO {

    private String name;
    private String slug;
    private Long parentId;
    private List<CategoryResponse> subcategories;
}
