package com.minzetsu.ecommerce.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminProductImageResponse extends BaseDTO {

    private Long productId;
    private String url;
    private Boolean isPrimary;
}
