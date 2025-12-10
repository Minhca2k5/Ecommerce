package com.minzetsu.ecommerce.user.controller.admin;

import com.minzetsu.ecommerce.user.dto.filter.AddressFilter;
import com.minzetsu.ecommerce.user.dto.response.AddressResponse;
import com.minzetsu.ecommerce.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Addresses", description = "Quản lý địa chỉ của người dùng (Address Management)")
public class AdminAddressController {

    private final AddressService addressService;

    @Operation(
            summary = "Lấy thông tin địa chỉ theo ID",
            description = "Trả về thông tin chi tiết của một địa chỉ cụ thể theo ID."
    )
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(
            @PathVariable("addressId") Long addressId
    ) {
        return ResponseEntity.ok(addressService.getAddressResponseById(addressId, null));
    }

    @Operation(
            summary = "Tìm kiếm danh sách địa chỉ",
            description = "Tìm kiếm và lọc danh sách địa chỉ theo điều kiện (userId, tỉnh/thành phố, quận/huyện, v.v.). Hỗ trợ phân trang."
    )
    @GetMapping
    public ResponseEntity<Page<AddressResponse>> searchAddresses(
            @ModelAttribute AddressFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(addressService.searchAddressResponses(filter, pageable));
    }

    @Operation(
            summary = "Lấy địa chỉ mặc định của người dùng",
            description = "Trả về địa chỉ được đánh dấu là mặc định (default address) của người dùng cụ thể."
    )
    @GetMapping("/user/{userId}/default")
    public ResponseEntity<AddressResponse> getDefaultAddressByUserId(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(addressService.getMyDefaultAddressResponse(userId));
    }
}
