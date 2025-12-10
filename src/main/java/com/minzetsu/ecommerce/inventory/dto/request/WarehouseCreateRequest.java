package com.minzetsu.ecommerce.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO dùng khi tạo hoặc cập nhật kho hàng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseCreateRequest {

    @NotBlank(message = "Warehouse code is required")
    private String code;

    @NotBlank(message = "Warehouse name is required")
    private String name;

    private String address;
    @NotBlank(message = "City is required")
    private String city;
    private String state;
    @NotBlank(message = "Country is required")
    private String country;
    private String zipcode;
    private String phone;

    private Boolean isActive = true;
}
