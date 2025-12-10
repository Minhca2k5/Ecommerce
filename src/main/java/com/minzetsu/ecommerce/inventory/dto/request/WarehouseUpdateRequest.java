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
public class WarehouseUpdateRequest {
    private String code;
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private String phone;
    private Boolean isActive;
}
