package com.minzetsu.ecommerce.user.service;

import com.minzetsu.ecommerce.user.dto.filter.AddressFilter;
import com.minzetsu.ecommerce.user.dto.request.AddressCreateRequest;
import com.minzetsu.ecommerce.user.dto.request.AddressUpdateRequest;
import com.minzetsu.ecommerce.user.dto.response.AddressResponse;
import com.minzetsu.ecommerce.user.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AddressService {
    void deleteAddress(Long addressId, Long currentUserId);
    Address getAddressByIsDefaultTrueAndUserId(Long userId);
    void updateIsDefaultById(Long addressId, Long userId);
    boolean existsById(Long id);
    boolean existsByUserId(Long userId);
    Address getAddressById(Long addressId);

    List<AddressResponse> getAddressResponsesByUserId(Long userId);
    AddressResponse getAddressResponseById(Long addressId, Long userId);
    AddressResponse getMyDefaultAddressResponse(Long userId);
    AddressResponse createAddressResponse(AddressCreateRequest request, Long userId);
    AddressResponse updateAddressResponse(AddressUpdateRequest request, Long addressId, Long userId);
    Page<AddressResponse> searchAddressResponses(AddressFilter addressFilter, Pageable pageable);

}
