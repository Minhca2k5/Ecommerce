package com.minzetsu.ecommerce.inventory.dto.response;

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
public class WarehouseResponse extends BaseDTO {

    private String code;
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private String phone;
    private Boolean isActive;

    private List<InventoryResponse> inventories;
}
