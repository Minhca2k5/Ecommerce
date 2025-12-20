package com.minzetsu.ecommerce.user.service.impl;

import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.user.dto.filter.AddressFilter;
import com.minzetsu.ecommerce.user.dto.request.AddressCreateRequest;
import com.minzetsu.ecommerce.user.dto.request.AddressUpdateRequest;
import com.minzetsu.ecommerce.user.dto.response.AddressResponse;
import com.minzetsu.ecommerce.user.entity.Address;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.user.mapper.AddressMapper;
import com.minzetsu.ecommerce.user.repository.AddressRepository;
import com.minzetsu.ecommerce.user.repository.AddressSpecification;
import com.minzetsu.ecommerce.user.service.AddressService;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final UserService userService;

    private Address getExistingAddress(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address not found with id: " + id));
    }

    private void validateUser(Long userId) {
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
    }

    private void validateOwnership(Address address, Long userId) {
        if (!address.getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("You do not have permission to modify this address");
        }
    }

    @Override
    @Transactional
    public void deleteAddress(Long id, Long currentUserId) {
        Address address = getExistingAddress(id);
        validateOwnership(address, currentUserId);
        addressRepository.delete(address);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddressById(Long addressId) {
        return getExistingAddress(addressId);
    }

    @Override
    public boolean existsById(Long id) {
        return addressRepository.existsById(id);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return addressRepository.existsByUserId(userId);
    }

    @Override
    @Transactional
    public void updateIsDefaultById(Long addressId, Long userId) {
        validateUser(userId);
        Address address = getExistingAddress(addressId);
        validateOwnership(address, userId);
        addressRepository.findByIsDefaultTrueAndUserId(userId)
                .ifPresent(currentDefault ->
                        addressRepository.updateAddressByDefault(false, currentDefault.getId()));
        addressRepository.updateAddressByDefault(true, addressId);
    }

    @Override
    @Transactional
    public AddressResponse createAddressResponse(AddressCreateRequest request, Long userId) {
        request.setUserId(userId);
        validateUser(userId);
        Address address = addressMapper.toEntity(request);
        User user = userService.getUserById(userId);
        address.setUser(user);
        addressRepository.findByIsDefaultTrueAndUserId(userId)
                .ifPresent(defaultAddress -> address.setIsDefault(false));
        Address savedAddress = addressRepository.save(address);
        return addressMapper.toResponse(savedAddress);
    }

    @Override
    @Transactional
    public AddressResponse updateAddressResponse(AddressUpdateRequest request, Long addressId, Long userId) {
        Address address = getExistingAddress(addressId);
        validateOwnership(address, userId);
        addressMapper.updateAddressFromRequest(request, address);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressResponsesByUserId(Long userId) {
        validateUser(userId);
        List<Address> addresses = addressRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return addressMapper.toResponseList(addresses);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressResponseById(Long addressId, Long userId) {
        Address address = getExistingAddress(addressId);
        if (userId != null) validateOwnership(address, userId);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddressByIsDefaultTrueAndUserId(Long userId) {
        return addressRepository.findByIsDefaultTrueAndUserId(userId)
                .orElseThrow(() -> new NotFoundException("Default address not found for userId: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getMyDefaultAddressResponse(Long userId) {
        Address address = getAddressByIsDefaultTrueAndUserId(userId);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AddressResponse> searchAddressResponses(AddressFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                addressRepository,
                AddressSpecification.filter(filter),
                addressMapper::toResponse
        );
    }
}
