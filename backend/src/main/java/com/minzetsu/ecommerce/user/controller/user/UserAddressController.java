package com.minzetsu.ecommerce.user.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.user.dto.request.AddressCreateRequest;
import com.minzetsu.ecommerce.user.dto.request.AddressUpdateRequest;
import com.minzetsu.ecommerce.user.dto.response.AddressResponse;
import com.minzetsu.ecommerce.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Addresses", description = "Quản lý địa chỉ của người dùng (Address Management)")
public class UserAddressController {

        private final AddressService addressService;

        @Operation(summary = "Lấy danh sách địa chỉ của tôi", description = "Trả về tất cả các địa chỉ đã được lưu của người dùng hiện tại.")
        @GetMapping
        public ResponseEntity<List<AddressResponse>> getMyAddresses() {
                Long userId = getCurrentUserId();
                return ResponseEntity.ok(addressService.getAddressResponsesByUserId(userId));
        }

        @Operation(summary = "Lấy địa chỉ mặc định của tôi", description = "Trả về địa chỉ được đánh dấu là mặc định của người dùng hiện tại.")
        @GetMapping("/default")
        public ResponseEntity<AddressResponse> getMyDefaultAddress() {
                Long userId = getCurrentUserId();
                return ResponseEntity.ok(addressService.getMyDefaultAddressResponse(userId));
        }

        @Operation(summary = "Tạo địa chỉ mới", description = "Thêm địa chỉ mới cho người dùng hiện tại. Có thể đặt làm mặc định nếu cần.")
        @PostMapping
        public ResponseEntity<AddressResponse> createMyAddress(
                        @Valid @RequestBody AddressCreateRequest request) {
                Long userId = getCurrentUserId();
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(addressService.createAddressResponse(request, userId));
        }

        @Operation(summary = "Cập nhật địa chỉ của tôi", description = "Cập nhật thông tin địa chỉ cụ thể của người dùng hiện tại.")
        @PutMapping("/{addressId}")
        public ResponseEntity<AddressResponse> updateMyAddress(
                        @PathVariable("addressId") Long addressId,
                        @Valid @RequestBody AddressUpdateRequest request) {
                Long userId = getCurrentUserId();
                return ResponseEntity.ok(addressService.updateAddressResponse(request, addressId, userId));
        }

        @Operation(summary = "Xóa địa chỉ của tôi", description = "Xóa một địa chỉ cụ thể thuộc người dùng hiện tại.")
        @DeleteMapping("/{addressId}")
        public ResponseEntity<Void> deleteMyAddress(
                        @PathVariable("addressId") Long addressId) {
                Long userId = getCurrentUserId();
                addressService.deleteAddress(addressId, userId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Đặt địa chỉ mặc định", description = "Đặt một địa chỉ cụ thể của người dùng làm địa chỉ mặc định.")
        @PatchMapping("/{addressId}/set-default")
        public ResponseEntity<Void> setMyDefaultAddress(
                        @PathVariable("addressId") Long addressId) {
                Long userId = getCurrentUserId();
                addressService.updateIsDefaultById(addressId, userId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Lấy địa chỉ của tôi theo ID", description = "Trả về chi tiết của một địa chỉ cụ thể thuộc người dùng hiện tại.")
        @GetMapping("/{addressId}")
        public ResponseEntity<AddressResponse> getMyAddressById(
                        @PathVariable("addressId") Long addressId) {
                Long userId = getCurrentUserId();
                return ResponseEntity.ok(addressService.getAddressResponseById(addressId, userId));
        }

        private Long getCurrentUserId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new com.minzetsu.ecommerce.common.exception.UnAuthorizedException("Unauthenticated");
                }
                return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
}
