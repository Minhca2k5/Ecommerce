package com.minzetsu.ecommerce.inventory.dto.filter;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseFilter implements SortableFilter {
    private String code;
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private String phone;
    private Boolean isActive;

    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime updatedFrom;
    private LocalDateTime updatedTo;

    private String sortBy;
    private String sortDirection;
}
